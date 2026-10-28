package br.pucminas.moedaestudantil.dto;

import java.util.List;

public record ExtratoResponse(int saldoMoedas, List<TransacaoResponse> transacoes) {
}
