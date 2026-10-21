package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.*;

public record EmpresaRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 255) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}", message = "CNPJ deve ter 14 dígitos")
        String cnpj,
        @NotBlank(message = "Login é obrigatório") @Size(min = 3, max = 50, message = "Login deve ter de 3 a 50 caracteres")
        String login,
        @Size(max = 100) String senha
) {
}
