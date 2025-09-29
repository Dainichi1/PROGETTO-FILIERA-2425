package unicam.filiera.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Modello di dominio per i post social.
 * Non ha annotazioni JPA.
 */
@Getter
@ToString
public class PostSocial {

    private final Long id;
    private final String autoreUsername;
    private final Integer idAcquisto;
    private final String nomeItem;
    private final String tipoItem;
    private final String titolo;
    private final String testo;
    private final LocalDateTime createdAt;

    private PostSocial(Builder b) {
        this.id = b.id;
        this.autoreUsername = b.autoreUsername;
        this.idAcquisto = b.idAcquisto;
        this.nomeItem = b.nomeItem;
        this.tipoItem = b.tipoItem;
        this.titolo = b.titolo;
        this.testo = b.testo;
        this.createdAt = b.createdAt;
    }

    public static class Builder {
        private Long id;
        private String autoreUsername;
        private Integer idAcquisto;
        private String nomeItem;
        private String tipoItem;
        private String titolo;
        private String testo;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder autoreUsername(String a) { this.autoreUsername = a; return this; }
        public Builder idAcquisto(Integer i) { this.idAcquisto = i; return this; }
        public Builder nomeItem(String n) { this.nomeItem = n; return this; }
        public Builder tipoItem(String t) { this.tipoItem = t; return this; }
        public Builder titolo(String t) { this.titolo = t; return this; }
        public Builder testo(String t) { this.testo = t; return this; }
        public Builder createdAt(LocalDateTime c) { this.createdAt = c; return this; }

        public PostSocial build() {
            if (autoreUsername == null || titolo == null || testo == null) {
                throw new IllegalStateException("Campi obbligatori mancanti per PostSocial");
            }
            return new PostSocial(this);
        }
    }
}
