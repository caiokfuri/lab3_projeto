package br.pucminas.moedaestudantil.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Na nuvem o banco é informado pela variável {@code DATABASE_URL} no formato dos provedores
 * ({@code postgresql://usuario:senha@host:porta/banco?params}), que o JDBC não aceita diretamente.
 * Quando ela existe, é convertida em {@code spring.datasource.url/username/password}, com prioridade
 * sobre o H2 configurado para execução local. Funciona com Render, Neon, Supabase etc.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) return;
        environment.getPropertySources().addFirst(new MapPropertySource("DATABASE_URL", converter(databaseUrl.trim())));
    }

    static Map<String, Object> converter(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL deve começar com postgresql://");
        }
        if (uri.getHost() == null) {
            throw new IllegalStateException("DATABASE_URL sem host válido");
        }

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) jdbc.append(':').append(uri.getPort());
        jdbc.append(uri.getRawPath());
        if (uri.getRawQuery() != null) jdbc.append('?').append(uri.getRawQuery());

        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("spring.datasource.url", jdbc.toString());
        if (uri.getUserInfo() != null) {
            String[] credenciais = uri.getUserInfo().split(":", 2);
            propriedades.put("spring.datasource.username", credenciais[0]);
            if (credenciais.length > 1) propriedades.put("spring.datasource.password", credenciais[1]);
        }
        return propriedades;
    }
}
