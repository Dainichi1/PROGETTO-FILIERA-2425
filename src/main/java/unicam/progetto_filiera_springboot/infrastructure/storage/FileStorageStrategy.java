package unicam.progetto_filiera_springboot.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageStrategy {
    /**
     * Salva i file nella sottocartella indicata, ritorna i nomi file salvati (univoci).
     */
    List<String> store(List<MultipartFile> files, String subfolder) throws IOException;

    /**
     * Carica un file come Resource dalla sottocartella indicata.
     */
    Resource load(String subfolder, String filename);

    /**
     * Cancella una lista di file dalla sottocartella (best effort).
     */
    void delete(List<String> filenames, String subfolder);
}
