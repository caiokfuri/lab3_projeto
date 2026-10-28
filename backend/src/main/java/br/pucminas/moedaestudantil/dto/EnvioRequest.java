package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.*;

public record EnvioRequest(
        @NotNull(message = "Selecione o aluno") Long alunoId,
        @NotNull(message = "Informe a quantidade de moedas") @Min(value = 1, message = "Envie pelo menos 1 moeda")
        Integer valor,
        @NotBlank(message = "O motivo do reconhecimento é obrigatório")
        @Size(max = 500, message = "Motivo deve ter até 500 caracteres")
        String motivo
) {
}
