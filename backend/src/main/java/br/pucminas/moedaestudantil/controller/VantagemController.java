package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.config.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.dto.UsuarioLogado;
import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.service.VantagemService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * Catálogo de vantagens (qualquer usuário logado consulta) e CRUD das vantagens da empresa logada.
 * Cadastro e edição recebem {@code multipart/form-data}: campos {@code nome}, {@code descricao},
 * {@code custoMoedas} e o arquivo {@code foto}.
 */
@RestController
@RequestMapping("/api/vantagens")
public class VantagemController {

    private final VantagemService vantagemService;

    public VantagemController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    @GetMapping
    public List<VantagemResponse> listar(@RequestParam(required = false) Long empresaId) {
        return vantagemService.listar(empresaId);
    }

    @GetMapping("/{id}")
    public VantagemResponse buscar(@PathVariable Long id) {
        return vantagemService.buscar(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VantagemResponse> cadastrar(
            @Valid @ModelAttribute VantagemRequest dados,
            @RequestPart(name = "foto", required = false) MultipartFile foto,
            @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.EMPRESA);
        VantagemResponse criada = vantagemService.cadastrar(usuario.id(), dados, foto);
        return ResponseEntity.created(URI.create("/api/vantagens/" + criada.id())).body(criada);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VantagemResponse atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute VantagemRequest dados,
            @RequestPart(name = "foto", required = false) MultipartFile foto,
            @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.EMPRESA);
        return vantagemService.atualizar(usuario.id(), id, dados, foto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id,
                                        @RequestAttribute(AutenticacaoInterceptor.USUARIO_LOGADO) UsuarioLogado usuario) {
        Acesso.exigirPerfil(usuario, Perfil.EMPRESA);
        vantagemService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
