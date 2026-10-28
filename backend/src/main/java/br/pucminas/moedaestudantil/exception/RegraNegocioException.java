package br.pucminas.moedaestudantil.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class RegraNegocioException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> campos;

    public RegraNegocioException(HttpStatus status, String mensagem) {
        this(status, mensagem, Map.of());
    }

    public RegraNegocioException(HttpStatus status, String mensagem, Map<String, String> campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos;
    }

    public static RegraNegocioException conflito(String campo, String mensagem) {
        return new RegraNegocioException(HttpStatus.CONFLICT, mensagem, Map.of(campo, mensagem));
    }

    public static RegraNegocioException naoAutenticado(String mensagem) {
        return new RegraNegocioException(HttpStatus.UNAUTHORIZED, mensagem);
    }

    public static RegraNegocioException acessoNegado() {
        return new RegraNegocioException(HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação");
    }

    public HttpStatus getStatus() { return status; }
    public Map<String, String> getCampos() { return campos; }
}
