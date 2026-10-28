package br.pucminas.moedaestudantil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessorTest {

    @Test
    void creditoSemestralEhAcumulativoEUnicoPorSemestre() {
        Professor professor = new Professor();

        assertThat(professor.creditarSemestre("2026/1")).isTrue();
        assertThat(professor.creditarSemestre("2026/1")).isFalse();
        assertThat(professor.getSaldoMoedas()).isEqualTo(1000);

        // saldo não distribuído é mantido e somado ao novo crédito
        assertThat(professor.creditarSemestre("2026/2")).isTrue();
        assertThat(professor.getSaldoMoedas()).isEqualTo(2000);
    }
}
