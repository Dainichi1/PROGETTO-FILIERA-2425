package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.service.PostSocialService;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class PostSocialController {

    private final PostSocialService service;

    @Autowired
    public PostSocialController(PostSocialService service) {
        this.service = service;
    }

    /**
     * Pubblica un nuovo post social legato a un item approvato
     */
    @PostMapping("/pubblica/{itemId}")
    public ResponseEntity<?> pubblica(@PathVariable Long itemId,
                                      @Valid @RequestBody PostSocialDto dto,
                                      Authentication auth) {
        try {
            // autore dalla sessione
            String autore = auth.getName();

            // delega al service (che usa factory + observer)
            PostSocialDto saved = service.pubblicaPost(itemId, autore, dto);

            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Restituisce tutti i post in ordine cronologico decrescente
     */
    @GetMapping
    public List<PostSocialDto> getAll() {
        return service.getAllPosts();
    }

    /**
     * Restituisce tutti i post pubblicati da un autore
     */
    @GetMapping("/autore/{username}")
    public List<PostSocialDto> getByAutore(@PathVariable String username) {
        return service.getPostsByAutore(username);
    }
}
