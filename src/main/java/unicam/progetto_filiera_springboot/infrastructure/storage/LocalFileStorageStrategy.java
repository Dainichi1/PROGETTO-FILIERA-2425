package unicam.progetto_filiera_springboot.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component("localFileStorageStrategy")
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final Path root;

    // mime-types consentiti: immagini e pdf (amplia se serve)
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );

    @Value("${storage.max-size-bytes:5242880}") // 5 MB default
    private long maxSize;

    public LocalFileStorageStrategy(@Value("${storage.local.root:./uploads}") String rootPath) throws IOException {
        this.root = Paths.get(rootPath).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public List<String> store(List<MultipartFile> files, String subfolder) throws IOException {
        if (files == null || files.isEmpty()) return List.of();

        Path targetDir = resolveSafeDir(subfolder);
        Files.createDirectories(targetDir);

        List<String> saved = new ArrayList<>();
        try {
            for (MultipartFile f : files) {
                validate(f);

                String ext = getExtension(Objects.requireNonNullElse(f.getOriginalFilename(), ""));
                String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
                Path dest = targetDir.resolve(filename).normalize();

                // blocca eventuali traversal post-normalize
                if (!dest.startsWith(targetDir)) {
                    throw new SecurityException("Path traversal rilevato");
                }

                Files.copy(f.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                saved.add(filename);
            }
        } catch (IOException | RuntimeException ex) {
            // rollback best-effort
            delete(saved, subfolder);
            throw ex;
        }
        return saved;
    }

    @Override
    public Resource load(String subfolder, String filename) {
        if (filename == null || filename.isBlank()) return null;

        Path dir = resolveSafeDir(subfolder);
        Path file = dir.resolve(safeFilename(filename)).normalize();

        // blocca traversal
        if (!file.startsWith(dir)) {
            return null;
        }

        if (!Files.exists(file) || !Files.isReadable(file) || Files.isDirectory(file)) {
            return null;
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(List<String> filenames, String subfolder) {
        if (filenames == null || filenames.isEmpty()) return;
        Path targetDir = resolveSafeDir(subfolder);
        for (String name : filenames) {
            try {
                Path p = targetDir.resolve(safeFilename(name)).normalize();
                if (p.startsWith(targetDir)) {
                    Files.deleteIfExists(p);
                }
            } catch (IOException ignored) { }
        }
    }

    // ---------- helper ----------

    private void validate(MultipartFile f) {
        if (f.isEmpty()) throw new IllegalArgumentException("File vuoto: " + f.getOriginalFilename());
        if (f.getSize() > maxSize) throw new IllegalArgumentException("File troppo grande: " + f.getOriginalFilename());
        String contentType = Optional.ofNullable(f.getContentType()).orElse("");
        if (!ALLOWED.contains(contentType)) {
            throw new IllegalArgumentException("Tipo file non consentito (" + contentType + "): " + f.getOriginalFilename());
        }
    }

    private String getExtension(String originalName) {
        String clean = StringUtils.getFilename(originalName);
        if (clean == null) return "";
        int dot = clean.lastIndexOf('.');
        return dot >= 0 ? clean.substring(dot + 1) : "";
    }

    private Path resolveSafeDir(String folder) {
        String s = folder == null ? "" : folder.replace("\\", "/");
        // pulizia semplice per evitare traversal
        while (s.contains("..")) s = s.replace("..", "");
        if (s.startsWith("/")) s = s.substring(1);
        return this.root.resolve(s).toAbsolutePath().normalize();
    }

    private String safeFilename(String name) {
        String clean = StringUtils.getFilename(name);
        return clean == null ? "" : clean.replace("/", "").replace("\\", "");
    }
}
