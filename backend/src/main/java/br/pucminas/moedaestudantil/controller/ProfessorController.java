package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.ProfessorResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.service.ProfessorService;
import org.springframework.web.bind.annotation.*;

/** Professores são pré-cadastrados pela instituição; aqui só há consulta do próprio perfil. */
@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping("/{id}")
    public ProfessorResponse buscar(@PathVariable Long id,
                                    @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.PROFESSOR, id);
        return professorService.buscar(id);
    }
}
