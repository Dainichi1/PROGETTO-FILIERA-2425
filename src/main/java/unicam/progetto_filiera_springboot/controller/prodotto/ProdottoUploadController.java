package unicam.progetto_filiera_springboot.controller.prodotto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;

import java.util.List;

@RestController
@RequestMapping("/api/prodotti")
public class ProdottoUploadController {

    private final ProdottoService prodottoService;

    public ProdottoUploadController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<ProdottoResponse> uploadFoto(@PathVariable Long id,
                                                       @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(prodottoService.uploadFoto(id, files));
    }

    @PostMapping("/{id}/certificati")
    public ResponseEntity<ProdottoResponse> uploadCertificati(@PathVariable Long id,
                                                              @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(prodottoService.uploadCertificati(id, files));
    }
}
