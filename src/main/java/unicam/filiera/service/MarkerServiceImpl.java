package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.MarkerDto;
import unicam.filiera.entity.MarkerEntity;
import unicam.filiera.repository.MarkerRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MarkerServiceImpl implements MarkerService {

    private final MarkerRepository markerRepository;

    @Autowired
    public MarkerServiceImpl(MarkerRepository markerRepository) {
        this.markerRepository = markerRepository;
    }

    @Override
    public MarkerDto saveMarker(MarkerDto dto) {
        // controllo duplicato sulla label
        Optional<MarkerEntity> existing = markerRepository.findByLabel(dto.getLabel());
        if (existing.isPresent()) {
            return mapToDto(existing.get()); // restituisco il marker già esistente
        }
        MarkerEntity saved = markerRepository.save(mapToEntity(dto));
        return mapToDto(saved);
    }

    @Override
    public List<MarkerDto> saveMarkers(List<MarkerDto> dtos) {
        return dtos.stream()
                .map(this::saveMarker) // usa logica di deduplica
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkerDto> getAllMarkers() {
        return markerRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void deleteMarker(Long id) {
        markerRepository.deleteById(id);
    }

    @Override
    public void clearMarkers() {
        markerRepository.deleteAll();
    }

    // =======================
    // MAPPER interni al Service
    // =======================

    private MarkerDto mapToDto(MarkerEntity e) {
        return new MarkerDto(
                e.getId(),
                e.getLat(),
                e.getLng(),
                e.getLabel(),
                e.getColor()
        );
    }

    private MarkerEntity mapToEntity(MarkerDto dto) {
        MarkerEntity e = new MarkerEntity();
        e.setId(dto.getId());
        e.setLat(dto.getLat());
        e.setLng(dto.getLng());
        e.setLabel(dto.getLabel());
        e.setColor(dto.getColor());
        return e;
    }
}
