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

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileStorageStrategy storage;

    public FileController(@Qualifier("localFileStorageStrategy") FileStorageStrategy storage) {
        this.storage = storage;
    }

    @GetMapping("/prodotti/{id}/{tipo}/{filename:.+}")
    public ResponseEntity<?> getFile(@PathVariable Long id,
                                     @PathVariable String tipo,     // "foto" | "certificati"
                                     @PathVariable String filename) {

        String subfolder = "prodotti/" + id + "/" + tipo;
        Resource res = storage.load(subfolder, filename);
        if (res == null) return ResponseEntity.notFound().build();

        // 1) Determina il content-type
        String contentType = detectContentType(res, filename);

        // 2) Content-Disposition inline + Content-Type corretto
        String encoded = UriUtils.encode(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encoded + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                .contentType(MediaType.parseMediaType(contentType))
                .body(res);
    }

    private String detectContentType(Resource res, String filename) {
        // fallback map per estensioni comuni
        Map<String, String> byExt = Map.of(
                "jpg", "image/jpeg",
                "jpeg", "image/jpeg",
                "png", "image/png",
                "gif", "image/gif",
                "webp", "image/webp",
                "pdf", "application/pdf"
        );

        // a) se è FileSystemResource prova Files.probeContentType
        try {
            if (res instanceof FileSystemResource fsr) {
                Path p = fsr.getFile().toPath();
                String probed = Files.probeContentType(p);
                if (probed != null) return probed;
            }
        } catch (Exception ignored) {}

        // b) prova con l’heuristic di Java sul nome
        String byName = URLConnection.guessContentTypeFromName(filename);
        if (byName != null) return byName;

        // c) fallback su estensione
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            ext = filename.substring(dot + 1).toLowerCase();
        }
        if (byExt.containsKey(ext)) return byExt.get(ext);

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
