package unicam.filiera.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/file")
public class FileController {

    @GetMapping("/{tipo}/{filename}")
    public ResponseEntity<FileSystemResource> getFile(
            @PathVariable String tipo,
            @PathVariable String filename) {

        File file = new File("uploads/" + tipo + "/" + filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);

        // Prova a rilevare il content type corretto
        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (Exception e) {
            contentType = null;
        }

        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
