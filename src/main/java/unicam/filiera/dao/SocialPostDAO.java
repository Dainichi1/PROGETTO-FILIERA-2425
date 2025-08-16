package unicam.filiera.dao;

import unicam.filiera.dto.PostSocialDto;

import java.util.List;

public interface SocialPostDAO {

    /**
     * Inserisce un nuovo post/recensione nel feed
     */
    void pubblicaPost(PostSocialDto post);

    /**
     * Ritorna tutti i post ordinati dal più recente
     */
    List<PostSocialDto> findAllOrderByDataDesc();

    /**
     * Ritorna tutti i post di un determinato autore
     */
    List<PostSocialDto> findByAutore(String username);


}
