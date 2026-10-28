package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.LoginRequest;
import br.pucminas.moedaestudantil.dto.LoginResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(AutenticacaoInterceptor.extrairToken(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UsuarioLogado usuarioAtual(@RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        return usuario;
    }
}
