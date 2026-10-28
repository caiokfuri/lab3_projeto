package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.dto.AlunoResumoResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Cadastro (público), consulta, edição e remoção da própria conta de aluno. Professores listam os alunos
 * da sua instituição para enviar moedas.
 */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public List<AlunoResumoResponse> listar(
            @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.PROFESSOR);
        return alunoService.listarDaInstituicaoDoProfessor(usuario.id());
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id,
                                @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.ALUNO, id);
        return alunoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest dados) {
        AlunoResponse criado = alunoService.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/alunos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest dados,
                                   @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.ALUNO, id);
        return alunoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id,
                                        @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.ALUNO, id);
        alunoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
