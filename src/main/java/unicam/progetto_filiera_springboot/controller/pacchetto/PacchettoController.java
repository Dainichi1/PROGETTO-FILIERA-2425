package unicam.progetto_filiera_springboot.controller.pacchetto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/pacchetti")
public class PacchettoController {

    private final PacchettoService pacchettoService;

    public PacchettoController(PacchettoService pacchettoService) {
        this.pacchettoService = pacchettoService;
    }

    @PostMapping("/crea")
    public ResponseEntity<PacchettoResponse> creaPacchetto(@Valid @ModelAttribute PacchettoForm form,
                                                           @RequestParam("foto") List<MultipartFile> foto,
                                                           @RequestParam("certificati") List<MultipartFile> certificati,
                                                           Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        PacchettoResponse resp = pacchettoService.creaPacchettoConFile(
                form,
                principal.getName(),
                foto,
                certificati
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()     // base
                .path("/api/pacchetti/{id}")  // canonical resource URL
                .buildAndExpand(resp.getId())
                .toUri();

        return ResponseEntity.created(location).body(resp); // 201 + Location
    }

    @GetMapping("/miei")
    public ResponseEntity<List<PacchettoResponse>> mieiPacchetti(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(pacchettoService.pacchettiDi(principal.getName()));
    }
}
