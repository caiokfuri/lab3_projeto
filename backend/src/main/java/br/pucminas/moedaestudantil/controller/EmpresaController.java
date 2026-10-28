package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.EmpresaRequest;
import br.pucminas.moedaestudantil.dto.EmpresaResponse;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Cadastro (público), consulta, edição e remoção da própria conta de empresa parceira. */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public EmpresaResponse buscar(@PathVariable Long id,
                                  @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.EMPRESA, id);
        return empresaService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest dados) {
        EmpresaResponse criada = empresaService.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/empresas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public EmpresaResponse atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest dados,
                                     @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.EMPRESA, id);
        return empresaService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id,
                                        @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirProprio(usuario, Perfil.EMPRESA, id);
        empresaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
