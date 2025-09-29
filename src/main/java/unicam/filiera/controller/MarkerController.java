package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.MarkerDto;
import unicam.filiera.service.MarkerService;

import java.util.List;

@RestController
@RequestMapping("/gestore/markers/api")
public class MarkerController {

    private final MarkerService markerService;

    @Autowired
    public MarkerController(MarkerService markerService) {
        this.markerService = markerService;
    }

    /**
     * Ritorna tutti i marker salvati
     */
    @GetMapping
    public ResponseEntity<List<MarkerDto>> getAllMarkers() {
        return ResponseEntity.ok(markerService.getAllMarkers());
    }

    /**
     * Salva un nuovo marker
     */
    @PostMapping
    public ResponseEntity<MarkerDto> saveMarker(@RequestBody MarkerDto dto) {
        return ResponseEntity.ok(markerService.saveMarker(dto));
    }

    /**
     * Salva più marker contemporaneamente
     */
    @PostMapping("/batch")
    public ResponseEntity<List<MarkerDto>> saveMarkers(@RequestBody List<MarkerDto> dtos) {
        return ResponseEntity.ok(markerService.saveMarkers(dtos));
    }

    /**
     * Elimina un marker per ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarker(@PathVariable Long id) {
        markerService.deleteMarker(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina tutti i marker
     */
    @DeleteMapping
    public ResponseEntity<Void> clearMarkers() {
        markerService.clearMarkers();
        return ResponseEntity.noContent().build();
    }
}
