package unicam.progetto_filiera_springboot.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component("localFileStorageStrategy")
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final Path root;

    // mime-types consentiti: immagini e pdf (puoi ampliare)
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

        Path targetDir = this.root.resolve(safeFolder(subfolder));
        Files.createDirectories(targetDir);

        List<String> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            validate(f);
            String ext = getExtension(Objects.requireNonNull(f.getOriginalFilename()));
            String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path dest = targetDir.resolve(filename);
            // salva atomico
            try {
                Files.copy(f.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                saved.add(filename);
            } catch (IOException ex) {
                // rollback i già salvati
                delete(saved, subfolder);
                throw ex;
            }
        }
        return saved;
    }

    @Override
    public void delete(List<String> filenames, String subfolder) {
        if (filenames == null || filenames.isEmpty()) return;
        Path targetDir = this.root.resolve(safeFolder(subfolder));
        for (String name : filenames) {
            try {
                Files.deleteIfExists(targetDir.resolve(name));
            } catch (IOException ignored) { }
        }
    }

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

    private String safeFolder(String folder) {
        String s = folder == null ? "" : folder;
        // evita path traversal
        s = s.replace("..", "").replace("\\", "/");
        if (s.startsWith("/")) s = s.substring(1);
        return s;
    }
}
