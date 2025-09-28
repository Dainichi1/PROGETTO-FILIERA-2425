package unicam.filiera.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.PathDto;
import unicam.filiera.entity.PathEntity;
import unicam.filiera.repository.PathRepository;
import unicam.filiera.service.PathService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PathServiceImpl implements PathService {

    private final PathRepository pathRepository;
    private final ObjectMapper mapper;

    @Autowired
    public PathServiceImpl(PathRepository pathRepository, ObjectMapper mapper) {
        this.pathRepository = pathRepository;
        this.mapper = mapper;
    }

    @Override
    public PathDto savePath(PathDto dto) {
        try {
            PathEntity entity = new PathEntity();
            entity.setProdottoTrasformatoId(dto.getProdottoTrasformatoId());
            entity.setCoordsJson(mapper.writeValueAsString(dto.getCoords()));

            PathEntity saved = pathRepository.save(entity);

            return new PathDto(
                    saved.getId(),
                    saved.getProdottoTrasformatoId(),
                    mapper.readValue(saved.getCoordsJson(), new TypeReference<>() {})
            );
        } catch (Exception e) {
            throw new RuntimeException("Errore salvataggio path", e);
        }
    }

    @Override
    public List<PathDto> getAllPaths() {
        return pathRepository.findAll().stream()
                .map(e -> {
                    try {
                        List<double[]> coords = mapper.readValue(e.getCoordsJson(), new TypeReference<>() {});
                        return new PathDto(e.getId(), e.getProdottoTrasformatoId(), coords);
                    } catch (Exception ex) {
                        throw new RuntimeException("Errore lettura coords JSON", ex);
                    }
                })
                .collect(Collectors.toList());
    }
}
