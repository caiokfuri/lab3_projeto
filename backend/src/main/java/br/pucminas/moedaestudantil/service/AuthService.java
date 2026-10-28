package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.UsuarioDAO;
import br.pucminas.moedaestudantil.dto.LoginRequest;
import br.pucminas.moedaestudantil.dto.LoginResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticação por login/senha com sessões identificadas por token opaco (enviado pelo front-end no
 * cabeçalho {@code Authorization: Bearer <token>}). As sessões ficam em memória: reiniciar o servidor
 * exige novo login.
 */
@Service
public class AuthService {

    private record Sessao(Long usuarioId, Instant expiraEm) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final Duration validade;
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public AuthService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder,
                       @Value("${app.auth.validade-horas:8}") long validadeHoras) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarioDAO.findByLogin(dados.login().trim())
                .filter(u -> u.autenticar(dados.senha(), passwordEncoder))
                .orElseThrow(() -> RegraNegocioException.naoAutenticado("Login ou senha inválidos"));

        String token = novoToken();
        sessoes.put(token, new Sessao(usuario.getId(), Instant.now().plus(validade)));
        return new LoginResponse(token, UsuarioLogado.from(usuario));
    }

    /** Resolve o usuário dono do token; falha com 401 se o token não existir ou tiver expirado. */
    @Transactional(readOnly = true)
    public UsuarioLogado usuarioDoToken(String token) {
        Sessao sessao = token == null ? null : sessoes.get(token);
        if (sessao == null || sessao.expiraEm().isBefore(Instant.now())) {
            if (token != null) sessoes.remove(token);
            throw RegraNegocioException.naoAutenticado("Sessão expirada ou inválida. Faça login novamente.");
        }
        return usuarioDAO.findById(sessao.usuarioId())
                .map(UsuarioLogado::from)
                .orElseThrow(() -> {
                    sessoes.remove(token);
                    return RegraNegocioException.naoAutenticado("Usuário não encontrado. Faça login novamente.");
                });
    }

    public void logout(String token) {
        if (token != null) sessoes.remove(token);
    }

    /** Usado quando a conta é removida. */
    public void encerrarSessoes(Long usuarioId) {
        sessoes.values().removeIf(s -> s.usuarioId().equals(usuarioId));
    }

    private static String novoToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
