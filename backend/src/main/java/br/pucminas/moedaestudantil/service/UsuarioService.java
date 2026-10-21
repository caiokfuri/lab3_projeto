package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.UsuarioDAO;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UsuarioService {

    static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public void aplicarCredenciais(Usuario usuario, String login, String email, String senha) {
        Long id = usuario.getId();
        String loginNormalizado = login.trim();
        String emailNormalizado = email.trim().toLowerCase();

        boolean loginEmUso = id == null
                ? usuarioDAO.existsByLogin(loginNormalizado)
                : usuarioDAO.existsByLoginAndIdNot(loginNormalizado, id);
        if (loginEmUso) {
            throw RegraNegocioException.conflito("login", "Login já está em uso");
        }

        boolean emailEmUso = id == null
                ? usuarioDAO.existsByEmail(emailNormalizado)
                : usuarioDAO.existsByEmailAndIdNot(emailNormalizado, id);
        if (emailEmUso) {
            throw RegraNegocioException.conflito("email", "E-mail já está cadastrado");
        }

        boolean senhaInformada = senha != null && !senha.isBlank();
        if (id == null && !senhaInformada) {
            throw campoInvalido("senha", "Senha é obrigatória");
        }
        if (senhaInformada) {
            if (senha.length() < TAMANHO_MINIMO_SENHA) {
                throw campoInvalido("senha", "Senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
            }
            usuario.setSenha(passwordEncoder.encode(senha));
        }

        usuario.setLogin(loginNormalizado);
        usuario.setEmail(emailNormalizado);
    }

    static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    static RegraNegocioException campoInvalido(String campo, String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, mensagem, Map.of(campo, mensagem));
    }
}
