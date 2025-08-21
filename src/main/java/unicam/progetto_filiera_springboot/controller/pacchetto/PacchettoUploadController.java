package unicam.progetto_filiera_springboot.controller.pacchetto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;

import java.util.List;

@RestController
@RequestMapping("/api/pacchetti")
public class PacchettoUploadController {

    private final PacchettoService pacchettoService;

    public PacchettoUploadController(PacchettoService pacchettoService) {
        this.pacchettoService = pacchettoService;
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<PacchettoResponse> uploadFoto(@PathVariable Long id,
                                                        @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(pacchettoService.uploadFoto(id, files));
    }

    @PostMapping("/{id}/certificati")
    public ResponseEntity<PacchettoResponse> uploadCertificati(@PathVariable Long id,
                                                               @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(pacchettoService.uploadCertificati(id, files));
    }
}
