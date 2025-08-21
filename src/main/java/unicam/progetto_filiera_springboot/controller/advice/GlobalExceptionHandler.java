package unicam.progetto_filiera_springboot.controller.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

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
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Richiesta non valida",
                "message", ex.getMessage()
        ));
    }

    // ===== Validazione: @Valid su @RequestBody / @ModelAttribute =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleManve(MethodArgumentNotValidException ex) {
        var binding = ex.getBindingResult();

        // field -> list of messages (se più errori sullo stesso campo)
        Map<String, List<String>> fieldErrors = binding.getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        fe -> fe.getField(),
                        LinkedHashMap::new,
                        Collectors.mapping(fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("non valido"),
                                Collectors.toList())
                ));

        // errori globali (object-level)
        List<String> globalErrors = binding.getGlobalErrors()
                .stream()
                .map(oe -> Optional.ofNullable(oe.getDefaultMessage()).orElse("Richiesta non valida"))
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validazione fallita");
        body.put("message", "Correggi i campi evidenziati e riprova.");
        body.put("fieldErrors", fieldErrors);
        if (!globalErrors.isEmpty()) body.put("globalErrors", globalErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // ===== Validazione: @Validated su parametri/metodi, Binding MVC e ValidationException custom =====
    @ExceptionHandler({ConstraintViolationException.class, BindException.class, ValidationException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(Exception ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validazione fallita");

        if (ex instanceof ConstraintViolationException cve) {
            // es: @RequestParam, @PathVariable, @Validated su metodo/service
            List<Map<String, String>> violations = cve.getConstraintViolations().stream()
                    .map(vi -> Map.of(
                            "path", Optional.ofNullable(vi.getPropertyPath()).map(Object::toString).orElse(""),
                            "message", Optional.ofNullable(vi.getMessage()).orElse("non valido")
                    ))
                    .toList();
            body.put("violations", violations);
            body.put("message", "Parametri non validi.");

        } else if (ex instanceof BindException be) {
            // es: errori su @ModelAttribute non coperti da MANVE
            Map<String, List<String>> fieldErrors = be.getFieldErrors().stream()
                    .collect(Collectors.groupingBy(
                            fe -> fe.getField(),
                            LinkedHashMap::new,
                            Collectors.mapping(fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("non valido"),
                                    Collectors.toList())
                    ));
            body.put("fieldErrors", fieldErrors);
            body.put("message", "Correggi i campi evidenziati e riprova.");

        } else if (ex instanceof ValidationException ve) {
            // la tua ValidationException (strategy.validation)
            body.put("message", Optional.ofNullable(ve.getMessage()).orElse("Richiesta non valida."));
        }

        return ResponseEntity.badRequest().body(body);
    }

    // ===== Stato illegale (es. transizione errata: NOT IN_ATTESA, upload dopo APPROVATO) =====
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Stato non valido",
                "message", ex.getMessage(),
                "howToFix", "Aggiorna la pagina e riprova. Se il problema persiste, verifica lo stato corrente dell’elemento."
        ));
    }
}
