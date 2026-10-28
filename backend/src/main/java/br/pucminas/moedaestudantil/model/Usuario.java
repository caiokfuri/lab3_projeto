package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash BCrypt da senha; nunca exposto pela API. */
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, unique = true)
    private String email;

    /** Confere a senha informada com o hash armazenado. */
    public boolean autenticar(String senhaInformada, PasswordEncoder encoder) {
        return senhaInformada != null && encoder.matches(senhaInformada, senha);
    }

    public abstract String getNome();

    public abstract Perfil getPerfil();

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
