package br.pucminas.moedaestudantil.dao;

import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso aos dados comuns a todos os perfis (login e e-mail são únicos no sistema inteiro). */
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByLoginAndIdNot(String login, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
