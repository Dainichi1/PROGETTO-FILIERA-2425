package unicam.filiera.util;

import unicam.filiera.dto.PostSocialDto;

public class ValidatoreRecensione {

    /**
     * Valida tutti i campi obbligatori di una recensione.
     * Lancia IllegalArgumentException se qualcosa non va.
     */
    public static void valida(PostSocialDto post) {
        if (post == null) {
            throw new IllegalArgumentException("⚠ Il post non può essere nullo");
        }

        if (post.getAutoreUsername() == null || post.getAutoreUsername().isBlank()) {
            throw new IllegalArgumentException("⚠ L'autore è obbligatorio");
        }

        if (post.getTitolo() == null || post.getTitolo().isBlank()) {
            throw new IllegalArgumentException("⚠ Il titolo è obbligatorio");
        }

        if (post.getTesto() == null || post.getTesto().isBlank()) {
            throw new IllegalArgumentException("⚠ Il testo della recensione è obbligatorio");
        }


        if (post.getNomeItem() != null && post.getNomeItem().length() > 255) {
            throw new IllegalArgumentException("⚠ Il nome dell'item è troppo lungo");
        }


    }
}
