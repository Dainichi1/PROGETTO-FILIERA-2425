package unicam.progetto_filiera_springboot.infrastructure.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageFactory {

    @Bean
    public FileStorageStrategy fileStorageStrategy(
            @Value("${storage.type:local}") String type,
            @Qualifier("localFileStorageStrategy") FileStorageStrategy local
            // qui potresti aggiungere @Qualifier("s3FileStorageStrategy") s3, ecc.
    ) {
        // Factory Method: seleziona l’implementazione in base a 'storage.type'
        return switch (type.toLowerCase()) {
            case "local" -> local;
            // case "s3" -> s3;
            default -> local; // fallback
        };
    }
}
