package unicam.filiera.service;

import unicam.filiera.dto.PostSocialDto;

import java.util.List;

public interface PostSocialService {

    /**
     * Pubblica un nuovo post social legato a un item approvato.
     * L'autore viene preso dall'utente autenticato.
     * Nome e tipo item vengono recuperati dal DB (factory).
     */
    PostSocialDto pubblicaPost(Long itemId, String autoreUsername, PostSocialDto dto);

    /**
     * Restituisce tutti i post ordinati dal più recente.
     */
    List<PostSocialDto> getAllPosts();

    /**
     * Restituisce tutti i post pubblicati da un determinato autore.
     */
    List<PostSocialDto> getPostsByAutore(String username);
}
