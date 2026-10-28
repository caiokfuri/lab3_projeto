package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Guarda as fotos das vantagens em disco ({@code app.uploads.dir}); servidas em {@code /uploads/**}. */
@Service
public class ArmazenamentoFotoService {

    public static final String PREFIXO_URL = "/uploads/";
    private static final Set<String> EXTENSOES = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoFotoService.class);

    private final Path diretorio;

    public ArmazenamentoFotoService(@Value("${app.uploads.dir}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    /** @return URL pública da foto salva */
    public String salvar(MultipartFile foto) {
        String extensao = extensao(foto.getOriginalFilename());
        String tipo = foto.getContentType();
        if (foto.isEmpty() || tipo == null || !tipo.startsWith("image/") || !EXTENSOES.contains(extensao)) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPG, PNG, GIF ou WEBP",
                    Map.of("foto", "Envie uma imagem JPG, PNG, GIF ou WEBP"));
        }

        String nome = UUID.randomUUID() + "." + extensao;
        try (InputStream entrada = foto.getInputStream()) {
            Files.createDirectories(diretorio);
            Files.copy(entrada, diretorio.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a foto", e);
        }
        return PREFIXO_URL + nome;
    }

    public void remover(String url) {
        if (url == null || !url.startsWith(PREFIXO_URL)) return;
        Path arquivo = diretorio.resolve(url.substring(PREFIXO_URL.length())).normalize();
        if (!arquivo.startsWith(diretorio)) return;
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            log.warn("Não foi possível remover a foto {}: {}", arquivo, e.getMessage());
        }
    }

    public Path getDiretorio() {
        return diretorio;
    }

    private static String extensao(String nomeArquivo) {
        if (nomeArquivo == null) return "";
        int ponto = nomeArquivo.lastIndexOf('.');
        return ponto < 0 ? "" : nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }
}
