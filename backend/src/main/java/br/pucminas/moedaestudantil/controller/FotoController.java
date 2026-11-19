package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.model.Foto;
import br.pucminas.moedaestudantil.service.FotoService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Imagens das vantagens. Rota pública: a tag {@code <img>} do navegador não envia o token de autenticação.
 * Cada foto nova recebe um id novo, então a resposta pode ficar em cache por tempo indeterminado.
 */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final FotoService fotoService;

    public FotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> buscar(@PathVariable Long id) {
        Foto foto = fotoService.buscar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipoConteudo()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(foto.getConteudo());
    }
}
