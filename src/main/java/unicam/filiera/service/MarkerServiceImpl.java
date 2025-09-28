package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public MarkerEntity saveMarker(MarkerEntity marker) {
        // 🔎 controllo duplicato sulla label
        Optional<MarkerEntity> existing = markerRepository.findByLabel(marker.getLabel());
        if (existing.isPresent()) {
            return existing.get(); // restituisco il marker già esistente
        }
        return markerRepository.save(marker);
    }

    @Override
    public List<MarkerEntity> saveMarkers(List<MarkerEntity> markers) {
        // salvo solo quelli non già presenti
        return markers.stream()
                .map(this::saveMarker) // usa logica di deduplica
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkerEntity> getAllMarkers() {
        return markerRepository.findAll();
    }

    @Override
    public void deleteMarker(Long id) {
        markerRepository.deleteById(id);
    }

    @Override
    public void clearMarkers() {
        markerRepository.deleteAll();
    }
}
