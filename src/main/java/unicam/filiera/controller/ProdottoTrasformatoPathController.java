package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.ProdottoTrasformatoPathDto;
import unicam.filiera.service.ProdottoTrasformatoPathService;

@RestController
@RequestMapping("/gestore/prodotti-trasformati/api")
public class ProdottoTrasformatoPathController {

    private final ProdottoTrasformatoPathService pathService;

    @Autowired
    public ProdottoTrasformatoPathController(ProdottoTrasformatoPathService pathService) {
        this.pathService = pathService;
    }

    /**
     * Ritorna il percorso di un prodotto trasformato,
     * con marker sia per il prodotto trasformato che per le fasi (prodotti origine).
     */
    @GetMapping("/{id}/path")
    public ResponseEntity<ProdottoTrasformatoPathDto> getPath(@PathVariable Long id) {
        ProdottoTrasformatoPathDto dto = pathService.getPath(id);
        return ResponseEntity.ok(dto);
    }
}
