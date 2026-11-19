package br.pucminas.moedaestudantil.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void converteUrlInternaDaRenderSemPorta() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.converter(
                "postgresql://moeda:s3nh%40forte@dpg-abc123-a/moeda_estudantil");

        assertThat(props).containsEntry("spring.datasource.url", "jdbc:postgresql://dpg-abc123-a/moeda_estudantil")
                .containsEntry("spring.datasource.username", "moeda")
                .containsEntry("spring.datasource.password", "s3nh@forte");
    }

    @Test
    void mantemPortaEParametrosComoSslmode() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.converter(
                "postgres://user:pw@ep-x.sa-east-1.aws.neon.tech:5432/neondb?sslmode=require");

        assertThat(props).containsEntry("spring.datasource.url",
                "jdbc:postgresql://ep-x.sa-east-1.aws.neon.tech:5432/neondb?sslmode=require");
    }

    @Test
    void recusaOutrosBancos() {
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.converter("mysql://u:p@host/db"))
                .isInstanceOf(IllegalStateException.class);
    }
}
