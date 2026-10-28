package br.pucminas.moedaestudantil;

import br.pucminas.moedaestudantil.config.DadosIniciais;
import br.pucminas.moedaestudantil.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fluxo completo: professor envia moedas, empresa cadastra vantagem, aluno troca e empresa confere o cupom. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:moeda;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.uploads.dir=target/test-uploads"
})
class MoedaIntegrationTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @SpyBean
    private EmailService emailService;

    private String token(String login, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("login", login, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(corpo).get("token").asText();
    }

    private JsonNode postJson(String url, String auth, Object corpo, int statusEsperado) throws Exception {
        var requisicao = post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo));
        if (auth != null) requisicao.header("Authorization", auth);
        String resposta = mvc.perform(requisicao)
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta);
    }

    private long instituicao(String nome) throws Exception {
        String corpo = mvc.perform(get("/api/instituicoes")).andReturn().getResponse().getContentAsString();
        for (JsonNode i : json.readTree(corpo)) {
            if (i.get("nome").asText().equals(nome)) return i.get("id").asLong();
        }
        throw new IllegalStateException(nome);
    }

    private long cadastrarAluno(String login, String cpf, String instituicao) throws Exception {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", "Aluno " + login);
        dados.put("email", login + "@exemplo.com");
        dados.put("cpf", cpf);
        dados.put("rg", "MG-1");
        dados.put("endereco", "Rua A, 1");
        dados.put("curso", "Engenharia de Software");
        dados.put("instituicaoId", instituicao(instituicao));
        dados.put("login", login);
        dados.put("senha", "segredo123");
        return postJson("/api/alunos", null, dados, 201).get("id").asLong();
    }

    private long cadastrarVantagem(String authEmpresa, String nome, int custo) throws Exception {
        String corpo = mvc.perform(multipart("/api/vantagens")
                        .file(new MockMultipartFile("foto", "foto.png", "image/png", PNG))
                        .param("nome", nome)
                        .param("descricao", "Descrição de " + nome)
                        .param("custoMoedas", String.valueOf(custo))
                        .header("Authorization", authEmpresa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fotoUrl").value(startsWith("/uploads/")))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asLong();
    }

    @Test
    void professoresPreCadastradosRecebemCreditoSemestral() throws Exception {
        String prof = token("prof.ana", DadosIniciais.SENHA_PROFESSORES);

        mvc.perform(get("/api/auth/me").header("Authorization", prof))
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"));
        mvc.perform(get("/api/transacoes/extrato").header("Authorization", prof))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoMoedas").value(1000))
                .andExpect(jsonPath("$.transacoes", hasSize(0)));
    }

    @Test
    void fluxoCompletoDeEnvioTrocaEConferencia() throws Exception {
        long alunoId = cadastrarAluno("joao", "11122233396", "PUC Minas");
        cadastrarAluno("outra.inst", "22233344405", "UFMG");
        String prof = token("prof.ana", DadosIniciais.SENHA_PROFESSORES);

        // professor só vê alunos da sua instituição
        mvc.perform(get("/api/alunos").header("Authorization", prof))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(alunoId))
                .andExpect(jsonPath("$[0].cpf").doesNotExist());

        JsonNode envio = postJson("/api/transacoes/envios", prof,
                Map.of("alunoId", alunoId, "valor", 300, "motivo", "Ótima participação em aula"), 201);
        verify(emailService).notificarRecebimento(any(), any());
        org.assertj.core.api.Assertions.assertThat(envio.get("tipo").asText()).isEqualTo("ENVIO");

        mvc.perform(get("/api/transacoes/extrato").header("Authorization", prof))
                .andExpect(jsonPath("$.saldoMoedas").value(700))
                .andExpect(jsonPath("$.transacoes[0].motivo").value("Ótima participação em aula"));

        // empresa cadastra vantagem e aluno troca
        postJson("/api/empresas", null, Map.of("nome", "Cantina", "email", "cantina@exemplo.com",
                "cnpj", "11222333000181", "login", "cantina", "senha", "segredo123"), 201);
        String empresa = token("cantina", "segredo123");
        long vantagemId = cadastrarVantagem(empresa, "Almoço grátis", 250);

        String aluno = token("joao", "segredo123");
        mvc.perform(get("/api/vantagens").header("Authorization", aluno))
                .andExpect(jsonPath("$[0].empresaNome").value("Cantina"));

        JsonNode resgate = postJson("/api/transacoes/resgates", aluno, Map.of("vantagemId", vantagemId), 201);
        String codigo = resgate.get("cupomCodigo").asText();
        org.assertj.core.api.Assertions.assertThat(codigo).matches("[A-Z2-9]{4}-[A-Z2-9]{4}");
        verify(emailService).enviarCupomAluno(any(), any());
        verify(emailService).enviarConferenciaParceiro(any(), any());

        mvc.perform(get("/api/transacoes/extrato").header("Authorization", aluno))
                .andExpect(jsonPath("$.saldoMoedas").value(50))
                .andExpect(jsonPath("$.transacoes", hasSize(2)))
                .andExpect(jsonPath("$.transacoes[*].tipo", containsInAnyOrder("ENVIO", "RESGATE")));

        // saldo insuficiente para trocar de novo
        postJson("/api/transacoes/resgates", aluno, Map.of("vantagemId", vantagemId), 422);

        // empresa confere o cupom (uma única vez)
        mvc.perform(get("/api/transacoes/resgates").header("Authorization", empresa))
                .andExpect(jsonPath("$[0].cupomCodigo").value(codigo))
                .andExpect(jsonPath("$[0].cupomUtilizado").value(false));
        mvc.perform(post("/api/transacoes/cupons/" + codigo.toLowerCase() + "/confirmacao").header("Authorization", empresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupomUtilizado").value(true));
        mvc.perform(post("/api/transacoes/cupons/" + codigo + "/confirmacao").header("Authorization", empresa))
                .andExpect(status().isConflict());

        // vantagem já resgatada não pode ser removida; aluno com transações também não
        mvc.perform(delete("/api/vantagens/" + vantagemId).header("Authorization", empresa))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/alunos/" + alunoId).header("Authorization", aluno))
                .andExpect(status().isConflict());
    }

    @Test
    void envioExigeSaldoMotivoEAlunoDaMesmaInstituicao() throws Exception {
        long alunoPuc = cadastrarAluno("joao", "11122233396", "PUC Minas");
        long alunoUfmg = cadastrarAluno("lia", "22233344405", "UFMG");
        String prof = token("prof.ana", DadosIniciais.SENHA_PROFESSORES);

        postJson("/api/transacoes/envios", prof, Map.of("alunoId", alunoPuc, "valor", 1001, "motivo", "x"), 422);
        mvc.perform(post("/api/transacoes/envios").header("Authorization", prof).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("alunoId", alunoPuc, "valor", 10, "motivo", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").exists());
        postJson("/api/transacoes/envios", prof, Map.of("alunoId", alunoUfmg, "valor", 10, "motivo", "Bom trabalho"), 400);

        // aluno não envia moedas
        postJson("/api/transacoes/envios", token("joao", "segredo123"),
                Map.of("alunoId", alunoPuc, "valor", 10, "motivo", "x"), 403);

        verify(emailService, never()).notificarRecebimento(any(), any());
    }

    @Test
    void empresaGerenciaApenasAsPropriasVantagens() throws Exception {
        postJson("/api/empresas", null, Map.of("nome", "Cantina", "email", "cantina@exemplo.com",
                "cnpj", "11222333000181", "login", "cantina", "senha", "segredo123"), 201);
        postJson("/api/empresas", null, Map.of("nome", "Papelaria", "email", "papel@exemplo.com",
                "cnpj", "44555666000172", "login", "papelaria", "senha", "segredo123"), 201);
        String cantina = token("cantina", "segredo123");
        String papelaria = token("papelaria", "segredo123");
        long vantagemId = cadastrarVantagem(cantina, "Café", 20);

        // edição sem foto mantém a atual
        mvc.perform(multipart("/api/vantagens/" + vantagemId)
                        .param("nome", "Café expresso").param("descricao", "Um café").param("custoMoedas", "25")
                        .with(r -> { r.setMethod("PUT"); return r; })
                        .header("Authorization", cantina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Café expresso"))
                .andExpect(jsonPath("$.fotoUrl").value(startsWith("/uploads/")));

        mvc.perform(delete("/api/vantagens/" + vantagemId).header("Authorization", papelaria))
                .andExpect(status().isForbidden());

        // validação: foto obrigatória no cadastro e custo positivo
        mvc.perform(multipart("/api/vantagens").param("nome", "X").param("descricao", "Y").param("custoMoedas", "0")
                        .header("Authorization", cantina))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.custoMoedas").exists());
        mvc.perform(multipart("/api/vantagens").param("nome", "X").param("descricao", "Y").param("custoMoedas", "5")
                        .header("Authorization", cantina))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.foto").exists());

        mvc.perform(delete("/api/vantagens/" + vantagemId).header("Authorization", cantina))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/vantagens").header("Authorization", cantina))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
