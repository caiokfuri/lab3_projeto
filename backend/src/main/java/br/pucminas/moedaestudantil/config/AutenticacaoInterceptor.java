package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exige um token válido em toda a API, exceto login, auto-cadastro de aluno/empresa e a lista de
 * instituições (usada no cadastro). O usuário resolvido fica no atributo {@link #USUARIO_LOGADO}
 * da requisição, lido pelos controllers com {@code @RequestAttribute}.
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    public static final String USUARIO_LOGADO = "usuarioLogado";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final AuthService authService;

    public AutenticacaoInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String metodo = request.getMethod();
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        if ("OPTIONS".equals(metodo) || rotaPublica(metodo, caminho)) {
            return true;
        }

        String token = extrairToken(request);
        if (token == null) {
            throw RegraNegocioException.naoAutenticado("Faça login para continuar");
        }
        request.setAttribute(USUARIO_LOGADO, authService.usuarioDoToken(token));
        return true;
    }

    public static String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO_BEARER)) return null;
        String token = cabecalho.substring(PREFIXO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static boolean rotaPublica(String metodo, String caminho) {
        return switch (metodo) {
            case "POST" -> caminho.equals("/api/auth/login") || caminho.equals("/api/alunos")
                    || caminho.equals("/api/empresas");
            case "GET" -> caminho.equals("/api/instituicoes");
            default -> false;
        };
    }
}
