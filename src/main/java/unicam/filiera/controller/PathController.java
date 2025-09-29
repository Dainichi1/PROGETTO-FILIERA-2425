package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PathDto;
import unicam.filiera.service.PathService;

import java.util.List;

@RestController
@RequestMapping("/gestore/paths/api")
public class PathController {

    private final PathService pathService;

    @Autowired
    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @GetMapping
    public ResponseEntity<List<PathDto>> getAllPaths() {
        return ResponseEntity.ok(pathService.getAllPaths());
    }

    @PostMapping
    public ResponseEntity<PathDto> savePath(@RequestBody PathDto dto) {
        return ResponseEntity.ok(pathService.savePath(dto));
    }
}
