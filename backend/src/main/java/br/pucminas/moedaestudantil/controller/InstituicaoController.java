package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dao.InstituicaoDAO;
import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Somente leitura: alimenta o seletor de instituição no cadastro de aluno. */
@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoController {

    private final InstituicaoDAO instituicaoDAO;

    public InstituicaoController(InstituicaoDAO instituicaoDAO) {
        this.instituicaoDAO = instituicaoDAO;
    }

    @GetMapping
    public List<InstituicaoResponse> listar() {
        return instituicaoDAO.findAllByOrderByNomeAsc().stream().map(InstituicaoResponse::from).toList();
    }
}
