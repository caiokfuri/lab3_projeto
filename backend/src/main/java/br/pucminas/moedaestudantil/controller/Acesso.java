package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Perfil;

/** Regras de autorização usadas pelos controllers (403 quando o usuário logado não pode agir). */
final class Acesso {

    private Acesso() {
    }

    static void exigirPerfil(UsuarioLogado usuario, Perfil perfil) {
        if (!usuario.eh(perfil)) {
            throw RegraNegocioException.acessoNegado();
        }
    }

    /** Somente o próprio usuário altera/consulta os dados completos da sua conta. */
    static void exigirProprio(UsuarioLogado usuario, Perfil perfil, Long id) {
        if (!usuario.eh(perfil) || !usuario.id().equals(id)) {
            throw RegraNegocioException.acessoNegado();
        }
    }
}
