package unicam.filiera.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import unicam.filiera.entity.PostSocialEntity;
import unicam.filiera.model.PostSocial;

import java.time.LocalDateTime;

/**
 * DTO per la creazione e visualizzazione dei Post Social.
 * Solo titolo e testo sono compilati dall’utente,
 * gli altri dati vengono ricostruiti dal service/factory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PostSocialDto {

    @EqualsAndHashCode.Include
    private Long id;

    private String autoreUsername;   // settato dal service
    private Integer idAcquisto;      // opzionale
    private String nomeItem;         // settato dal service
    private String tipoItem;         // settato dal service

    @NotBlank(message = "⚠ Il titolo è obbligatorio")
    @Size(max = 100, message = "⚠ Il titolo non può superare i 100 caratteri")
    private String titolo;

    @NotBlank(message = "⚠ Il testo è obbligatorio")
    @Size(max = 1000, message = "⚠ Il testo non può superare i 1000 caratteri")
    private String testo;

    private LocalDateTime createdAt;

    // ===== Mapping =====
    public static PostSocialDto fromEntity(PostSocialEntity entity) {
        return fromModel(entity.toModel());
    }

    public static PostSocialDto fromModel(PostSocial model) {
        PostSocialDto dto = new PostSocialDto();
        dto.setId(model.getId());
        dto.setAutoreUsername(model.getAutoreUsername());
        dto.setIdAcquisto(model.getIdAcquisto());
        dto.setNomeItem(model.getNomeItem());
        dto.setTipoItem(model.getTipoItem());
        dto.setTitolo(model.getTitolo());
        dto.setTesto(model.getTesto());
        dto.setCreatedAt(model.getCreatedAt());
        return dto;
    }

    /**
     * Non usare per la creazione in runtime.
     * Usa PostSocialFactory invece.
     */
    @Deprecated
    public static PostSocialEntity toEntity(PostSocialDto dto) {
        PostSocialEntity entity = new PostSocialEntity();
        entity.setId(dto.getId());
        entity.setAutoreUsername(dto.getAutoreUsername());
        entity.setIdAcquisto(dto.getIdAcquisto());
        entity.setNomeItem(dto.getNomeItem());
        entity.setTipoItem(dto.getTipoItem());
        entity.setTitolo(dto.getTitolo());
        entity.setTesto(dto.getTesto());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}
