package unicam.progetto_filiera_springboot.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageStrategy {
    /**
     * Salva i file nella sottocartella indicata, ritorna i nomi file salvati.
     * Deve essere idempotente sul nome (evitare collisioni).
     */
    List<String> store(List<MultipartFile> files, String subfolder) throws IOException;

    /**
     * Cancella una lista di file dalla sottocartella (best effort).
     */
    void delete(List<String> filenames, String subfolder);
}
