package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.FotoDAO;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Foto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;

/** Valida as imagens enviadas pelas empresas e entrega as fotos guardadas no banco. */
@Service
public class FotoService {

    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final FotoDAO fotoDAO;

    public FotoService(FotoDAO fotoDAO) {
        this.fotoDAO = fotoDAO;
    }

    /** Cria a foto a partir do arquivo enviado; ela é gravada junto com a vantagem (cascade). */
    public Foto criar(MultipartFile arquivo) {
        String tipo = arquivo.getContentType();
        if (arquivo.isEmpty() || tipo == null || !TIPOS.contains(tipo.toLowerCase())) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPG, PNG, GIF ou WEBP",
                    Map.of("foto", "Envie uma imagem JPG, PNG, GIF ou WEBP"));
        }
        if (arquivo.getSize() > Foto.TAMANHO_MAXIMO) {
            throw new RegraNegocioException(HttpStatus.PAYLOAD_TOO_LARGE, "A foto deve ter no máximo 5 MB",
                    Map.of("foto", "A foto deve ter no máximo 5 MB"));
        }
        try {
            return new Foto(tipo.toLowerCase(), arquivo.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto enviada", e);
        }
    }

    @Transactional(readOnly = true)
    public Foto buscar(Long id) {
        return fotoDAO.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Foto", id));
    }
}
