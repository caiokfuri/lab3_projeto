package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.*;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.service.MoedaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final MoedaService moedaService;

    public TransacaoController(MoedaService moedaService) {
        this.moedaService = moedaService;
    }

    /** Professor envia moedas a um aluno da sua instituição. */
    @PostMapping("/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse enviar(@Valid @RequestBody EnvioRequest dados,
                                    @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.PROFESSOR);
        return moedaService.enviarMoedas(usuario.id(), dados);
    }

    /** Aluno troca moedas por uma vantagem; a resposta traz o código do cupom. */
    @PostMapping("/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse resgatar(@Valid @RequestBody ResgateRequest dados,
                                      @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.ALUNO);
        return moedaService.resgatarVantagem(usuario.id(), dados.vantagemId());
    }

    /** Saldo e transações do aluno ou professor logado. */
    @GetMapping("/extrato")
    public ExtratoResponse extrato(@RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        return moedaService.consultarExtrato(usuario);
    }

    /** Empresa: resgates das suas vantagens, para conferência dos cupons. */
    @GetMapping("/resgates")
    public List<TransacaoResponse> resgatesDaEmpresa(
            @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.EMPRESA);
        return moedaService.listarResgatesDaEmpresa(usuario.id());
    }

    /** Empresa confere o código apresentado pelo aluno e marca o cupom como utilizado. */
    @PostMapping("/cupons/{codigo}/confirmacao")
    public TransacaoResponse confirmarCupom(@PathVariable String codigo,
                                            @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.EMPRESA);
        return moedaService.confirmarCupom(usuario.id(), codigo);
    }
}
