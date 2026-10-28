package br.pucminas.moedaestudantil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.uploads.dir=target/test-uploads"
})
class CrudIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private Map<String, Object> aluno() {
        long instituicaoId = primeiraInstituicao();
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", "Maria Silva");
        dados.put("email", "maria@exemplo.com");
        dados.put("cpf", "123.456.789-09");
        dados.put("rg", "MG-12.345.678");
        dados.put("endereco", "Rua A, 100");
        dados.put("curso", "Engenharia de Software");
        dados.put("instituicaoId", instituicaoId);
        dados.put("login", "maria");
        dados.put("senha", "segredo123");
        return dados;
    }

    private Map<String, Object> empresa() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", "Livraria Central");
        dados.put("email", "contato@livraria.com");
        dados.put("cnpj", "12.345.678/0001-90");
        dados.put("login", "livraria");
        dados.put("senha", "segredo123");
        return dados;
    }

    private long primeiraInstituicao() {
        try {
            String corpo = mvc.perform(get("/api/instituicoes")).andReturn().getResponse().getContentAsString();
            return json.readTree(corpo).get(0).get("id").asLong();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private long criar(String url, Map<String, Object> dados) throws Exception {
        String corpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(corpo);
        return node.get("id").asLong();
    }

    private String token(String login, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("login", login, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(corpo).get("token").asText();
    }

    @Test
    void crudCompletoDeAluno() throws Exception {
        long id = criar("/api/alunos", aluno());
        String auth = token("maria", "segredo123");

        mvc.perform(get("/api/alunos/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.saldoMoedas").value(0))
                .andExpect(jsonPath("$.instituicaoNome").isNotEmpty())
                .andExpect(jsonPath("$.senha").doesNotExist());

        Map<String, Object> edicao = aluno();
        edicao.put("curso", "Ciência da Computação");
        edicao.put("senha", "");
        mvc.perform(put("/api/alunos/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(edicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curso").value("Ciência da Computação"));

        mvc.perform(delete("/api/alunos/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        // a conta removida encerra a sessão
        mvc.perform(get("/api/alunos/" + id).header("Authorization", auth)).andExpect(status().isUnauthorized());
    }

    @Test
    void alunoComCpfOuLoginDuplicadoRetornaConflito() throws Exception {
        criar("/api/alunos", aluno());

        Map<String, Object> outro = aluno();
        outro.put("email", "outra@exemplo.com");
        outro.put("login", "outra");
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(outro)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.cpf").exists());

        outro.put("cpf", "98765432100");
        outro.put("login", "maria");
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(outro)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.login").exists());
    }

    @Test
    void alunoInvalidoRetornaErrosPorCampo() throws Exception {
        Map<String, Object> dados = aluno();
        dados.put("email", "invalido");
        dados.put("cpf", "123");
        dados.put("nome", "");
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.nome").exists());

        Map<String, Object> semSenha = aluno();
        semSenha.remove("senha");
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(semSenha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void crudCompletoDeEmpresa() throws Exception {
        long id = criar("/api/empresas", empresa());
        String auth = token("livraria", "segredo123");

        mvc.perform(get("/api/empresas/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("12345678000190"))
                .andExpect(jsonPath("$.quantidadeVantagens").value(0));

        Map<String, Object> edicao = empresa();
        edicao.put("nome", "Livraria Central Ltda");
        edicao.remove("senha");
        mvc.perform(put("/api/empresas/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(edicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Livraria Central Ltda"));

        mvc.perform(get("/api/empresas").header("Authorization", auth)).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/empresas/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("login", "livraria", "senha", "segredo123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEhUnicoEntrePerfis() throws Exception {
        criar("/api/alunos", aluno());

        Map<String, Object> dados = empresa();
        dados.put("login", "maria");
        mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.login").exists());
    }

    @Test
    void usuarioSoAlteraAPropriaConta() throws Exception {
        long idMaria = criar("/api/alunos", aluno());
        long idEmpresa = criar("/api/empresas", empresa());
        String authEmpresa = token("livraria", "segredo123");

        mvc.perform(get("/api/alunos/" + idMaria)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/alunos/" + idMaria).header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/alunos/" + idMaria).header("Authorization", authEmpresa))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/empresas/" + idEmpresa).header("Authorization", token("maria", "segredo123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        criar("/api/alunos", aluno());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("login", "maria", "senha", "errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));
    }
}
