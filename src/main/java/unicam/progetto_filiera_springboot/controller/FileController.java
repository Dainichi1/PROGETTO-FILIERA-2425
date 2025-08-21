package unicam.progetto_filiera_springboot.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import unicam.progetto_filiera_springboot.infrastructure.storage.FileStorageStrategy;

import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/files")
public class FileController {

    private static final Set<String> SEZIONI = Set.of("prodotti", "pacchetti");
    private static final Set<String> TIPI = Set.of("foto", "certificati");

    private final FileStorageStrategy storage;

    public FileController(@Qualifier("localFileStorageStrategy") FileStorageStrategy storage) {
        this.storage = storage;
    }

    /**
     * Endpoint unico per:
     *  - /files/prodotti/{id}/foto/{filename}
     *  - /files/prodotti/{id}/certificati/{filename}
     *  - /files/pacchetti/{id}/foto/{filename}
     *  - /files/pacchetti/{id}/certificati/{filename}
     */
    @GetMapping("/{sezione}/{id}/{tipo}/{filename:.+}")
    public ResponseEntity<?> getFile(@PathVariable String sezione,
                                     @PathVariable Long id,
                                     @PathVariable String tipo,
                                     @PathVariable String filename) {

        // Whitelist semplice per evitare percorsi non previsti
        if (!SEZIONI.contains(sezione) || !TIPI.contains(tipo)) {
            return ResponseEntity.notFound().build();
        }

        String subfolder = sezione + "/" + id + "/" + tipo;
        Resource res = storage.load(subfolder, filename);
        if (res == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = detectContentType(res, filename);
        String encoded = UriUtils.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encoded + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                .contentType(MediaType.parseMediaType(contentType))
                .body(res);
    }

    // ---------- helper ----------

    private String detectContentType(Resource res, String filename) {
        Map<String, String> byExt = Map.of(
                "jpg", "image/jpeg",
                "jpeg", "image/jpeg",
                "png", "image/png",
                "gif", "image/gif",
                "webp", "image/webp",
                "pdf", "application/pdf"
        );

        try {
            if (res instanceof FileSystemResource fsr) {
                Path p = fsr.getFile().toPath();
                String probed = Files.probeContentType(p);
                if (probed != null) return probed;
            }
        } catch (Exception ignored) {}

        String byName = URLConnection.guessContentTypeFromName(filename);
        if (byName != null) return byName;

        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            ext = filename.substring(dot + 1).toLowerCase();
        }
        return byExt.getOrDefault(ext, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
}
