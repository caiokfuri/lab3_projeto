package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.*;

/** Campos de texto do formulário multipart de vantagem; a foto chega como arquivo separado. */
public record VantagemRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 100, message = "Nome deve ter até 100 caracteres")
        String nome,
        @NotBlank(message = "Descrição é obrigatória") @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
        String descricao,
        @NotNull(message = "Custo é obrigatório") @Min(value = 1, message = "Custo deve ser de pelo menos 1 moeda")
        @Max(value = 100000, message = "Custo deve ser de no máximo 100.000 moedas")
        Integer custoMoedas
) {
}
