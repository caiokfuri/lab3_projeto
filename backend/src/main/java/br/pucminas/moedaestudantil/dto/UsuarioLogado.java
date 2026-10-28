package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.model.Usuario;

/** Usuário dono do token da requisição; injetado nos controllers protegidos. */
public record UsuarioLogado(Long id, String nome, String email, String login, Perfil perfil) {

    public static UsuarioLogado from(Usuario u) {
        return new UsuarioLogado(u.getId(), u.getNome(), u.getEmail(), u.getLogin(), u.getPerfil());
    }

    public boolean eh(Perfil p) {
        return perfil == p;
    }
}
