package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.GeocodedIndirizzoDto;
import unicam.filiera.dto.IndirizzoDto;
import unicam.filiera.service.IndirizzoService;

import java.util.List;

/**
 * Controller REST che fornisce gli indirizzi dei contenuti
 * (Prodotti, Pacchetti, Fiere, Visite, Trasformati).
 */
@RestController
@RequestMapping("/gestore/indirizzi/api")
public class IndirizzoController {

    private final IndirizzoService indirizzoService;

    @Autowired
    public IndirizzoController(IndirizzoService indirizzoService) {
        this.indirizzoService = indirizzoService;
    }

    @GetMapping
    public ResponseEntity<List<IndirizzoDto>> getIndirizzi() {
        List<IndirizzoDto> indirizzi = indirizzoService.getIndirizziDisponibili();
        return ResponseEntity.ok(indirizzi);
    }

    @PostMapping("/geocode")
    public ResponseEntity<List<GeocodedIndirizzoDto>> geocodeIndirizzi(@RequestBody List<IndirizzoDto> selezionati) {
        List<GeocodedIndirizzoDto> geocoded = selezionati.stream()
                .map(indirizzoService::geocodeIndirizzo)
                .toList();
        return ResponseEntity.ok(geocoded);
    }
}
