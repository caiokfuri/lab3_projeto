package br.pucminas.moedaestudantil.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envio de e-mail pela API HTTP da Brevo. Usado na nuvem: o plano gratuito da Render bloqueia as portas
 * SMTP (25, 465 e 587). Ativado quando {@code app.email.brevo-api-key} está definida.
 */
@Component
public class BrevoEmailClient {

    private static final String URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient http;
    private final String apiKey;

    public BrevoEmailClient(@Value("${app.email.brevo-api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(fabrica).build();
        this.apiKey = apiKey;
    }

    public boolean configurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** @throws org.springframework.web.client.RestClientException se a Brevo recusar ou estiver indisponível */
    public void enviar(String remetente, String para, String assunto, String texto) {
        http.post()
                .uri(URL)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "sender", Map.of("name", "Moeda Estudantil", "email", remetente),
                        "to", List.of(Map.of("email", para)),
                        "subject", assunto,
                        "textContent", texto))
                .retrieve()
                .toBodilessEntity();
    }
}
