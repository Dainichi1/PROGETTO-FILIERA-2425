package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.MarkerDto;
import unicam.filiera.entity.MarkerEntity;
import unicam.filiera.service.MarkerService;

import java.util.List;
import java.util.stream.Collectors;

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
        List<MarkerDto> markers = markerService.getAllMarkers().stream()
                .map(m -> new MarkerDto(
                        m.getId(),
                        m.getLat(),
                        m.getLng(),
                        m.getLabel(),
                        m.getColor()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(markers);
    }

    /**
     * Salva un nuovo marker
     */
    @PostMapping
    public ResponseEntity<MarkerDto> saveMarker(@RequestBody MarkerDto dto) {
        MarkerEntity entity = new MarkerEntity();
        entity.setLat(dto.getLat());
        entity.setLng(dto.getLng());
        entity.setLabel(dto.getLabel());
        entity.setColor(dto.getColor());

        MarkerEntity saved = markerService.saveMarker(entity);

        return ResponseEntity.ok(new MarkerDto(
                saved.getId(),
                saved.getLat(),
                saved.getLng(),
                saved.getLabel(),
                saved.getColor()
        ));
    }

    /**
     * Salva più marker contemporaneamente
     */
    @PostMapping("/batch")
    public ResponseEntity<List<MarkerDto>> saveMarkers(@RequestBody List<MarkerDto> dtos) {
        List<MarkerEntity> entities = dtos.stream().map(dto -> {
            MarkerEntity entity = new MarkerEntity();
            entity.setLat(dto.getLat());
            entity.setLng(dto.getLng());
            entity.setLabel(dto.getLabel());
            entity.setColor(dto.getColor());
            return entity;
        }).collect(Collectors.toList());

        List<MarkerDto> saved = markerService.saveMarkers(entities).stream()
                .map(m -> new MarkerDto(
                        m.getId(),
                        m.getLat(),
                        m.getLng(),
                        m.getLabel(),
                        m.getColor()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(saved);
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
