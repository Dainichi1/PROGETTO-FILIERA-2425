package unicam.filiera.factory;

import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.entity.PostSocialEntity;
import unicam.filiera.model.Item;

public final class PostSocialFactory {

    private PostSocialFactory() { }

    /**
     * Crea una nuova entità PostSocial a partire da:
     * - i dati del form (titolo, testo)
     * - l’item recuperato (nome, tipo)
     * - l’utente autenticato
     */
    public static PostSocialEntity creaPost(PostSocialDto dto, Item item, String autoreUsername) {
        return PostSocialEntity.builder()
                .autoreUsername(autoreUsername)
                .nomeItem(item.getNome())
                .tipoItem(item.getTipo().name())
                .titolo(dto.getTitolo())
                .testo(dto.getTesto())
                .build();
    }
}
