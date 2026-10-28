package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Vantagem;

public record VantagemResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        int custoMoedas,
        Long empresaId,
        String empresaNome
) {

    public static VantagemResponse from(Vantagem v) {
        return new VantagemResponse(v.getId(), v.getNome(), v.getDescricao(), v.getFotoUrl(), v.getCustoMoedas(),
                v.getEmpresa().getId(), v.getEmpresa().getNome());
    }
}
