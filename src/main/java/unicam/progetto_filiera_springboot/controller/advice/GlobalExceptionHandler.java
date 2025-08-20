package unicam.progetto_filiera_springboot.controller.advice;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import unicam.progetto_filiera_springboot.controller.error.UploadException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<Map<String, Object>> handleUpload(UploadException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore durante l'upload dei file",
                "message", ex.getMessage(),
                "howToFix", "Controlla formato (jpg/png/webp/pdf) e dimensione. Riprova l’upload."
        ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "error", "Dimensione massima superata",
                "message", "Uno o più file superano il limite consentito.",
                "howToFix", "Riduci la dimensione dei file o caricali separatamente."
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Richiesta non valida", "message", ex.getMessage()));
    }
}
