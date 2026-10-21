package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.*;

public record AlunoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 255) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF deve ter 11 dígitos")
        String cpf,
        @NotBlank(message = "RG é obrigatório") @Size(max = 20) String rg,
        @NotBlank(message = "Endereço é obrigatório") @Size(max = 255) String endereco,
        @NotBlank(message = "Curso é obrigatório") @Size(max = 255) String curso,
        @NotNull(message = "Instituição é obrigatória") Long instituicaoId,
        @NotBlank(message = "Login é obrigatório") @Size(min = 3, max = 50, message = "Login deve ter de 3 a 50 caracteres")
        String login,
        @Size(max = 100) String senha
) {
}
