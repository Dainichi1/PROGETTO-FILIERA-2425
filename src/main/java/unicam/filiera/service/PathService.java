package unicam.filiera.service;

import unicam.filiera.dto.PathDto;

import java.util.List;

public interface PathService {
    PathDto savePath(PathDto dto);
    List<PathDto> getAllPaths();
}
