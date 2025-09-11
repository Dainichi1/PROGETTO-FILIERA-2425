package unicam.filiera.entity;

import jakarta.persistence.*;
import lombok.*;
import unicam.filiera.model.PostSocial;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSocialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "autore_username", nullable = false, length = 50)
    private String autoreUsername;

    @Column(name = "id_acquisto")
    private Integer idAcquisto; // opzionale

    @Column(name = "nome_item", nullable = false, length = 100)
    private String nomeItem;

    @Column(name = "tipo_item", nullable = false, length = 30)
    private String tipoItem; // PRODOTTO | PACCHETTO | TRASFORMATO

    @Column(name = "titolo", nullable = false, length = 100)
    private String titolo;

    @Column(name = "testo", nullable = false, length = 1000)
    private String testo;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** Conversione a Domain Model */
    public PostSocial toModel() {
        return new PostSocial.Builder()
                .id(id)
                .autoreUsername(autoreUsername)
                .idAcquisto(idAcquisto)
                .nomeItem(nomeItem)
                .tipoItem(tipoItem)
                .titolo(titolo)
                .testo(testo)
                .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                .build();
    }
}
