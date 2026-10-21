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

    public HttpStatus getStatus() { return status; }
    public Map<String, String> getCampos() { return campos; }
}
