package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

public record AlunoResponse(
        Long id,
        String nome,
        String email,
        String login,
        String cpf,
        String rg,
        String endereco,
        String curso,
        int saldoMoedas,
        Long instituicaoId,
        String instituicaoNome
) {

    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getId(), a.getNome(), a.getEmail(), a.getLogin(), a.getCpf(), a.getRg(),
                a.getEndereco(), a.getCurso(), a.getSaldoMoedas(),
                a.getInstituicao().getId(), a.getInstituicao().getNome());
    }
}
