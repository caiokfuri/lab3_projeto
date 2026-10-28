package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** Dados do aluno visíveis ao professor na hora de escolher quem recebe moedas (sem documentos/endereço). */
public record AlunoResumoResponse(Long id, String nome, String email, String curso, String instituicaoNome) {

    public static AlunoResumoResponse from(Aluno a) {
        return new AlunoResumoResponse(a.getId(), a.getNome(), a.getEmail(), a.getCurso(), a.getInstituicao().getNome());
    }
}
