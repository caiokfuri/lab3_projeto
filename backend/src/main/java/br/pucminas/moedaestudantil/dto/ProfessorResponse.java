package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Professor;

public record ProfessorResponse(
        Long id,
        String nome,
        String email,
        String login,
        String cpf,
        String departamento,
        int saldoMoedas,
        Long instituicaoId,
        String instituicaoNome
) {

    public static ProfessorResponse from(Professor p) {
        return new ProfessorResponse(p.getId(), p.getNome(), p.getEmail(), p.getLogin(), p.getCpf(),
                p.getDepartamento(), p.getSaldoMoedas(), p.getInstituicao().getId(), p.getInstituicao().getNome());
    }
}
