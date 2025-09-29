package unicam.filiera.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class FaseProduzioneEmbeddable {

    @Column(nullable = false)
    private String descrizioneFase;

    @Column(nullable = false)
    private String produttoreUsername;

    @Column(nullable = false)
    private Long prodottoOrigineId;

    // Costruttore vuoto richiesto da JPA
    protected FaseProduzioneEmbeddable() {}

    // Costruttore completo per il mapping
    public FaseProduzioneEmbeddable(String descrizioneFase, String produttoreUsername, Long prodottoOrigineId) {
        this.descrizioneFase = descrizioneFase;
        this.produttoreUsername = produttoreUsername;
        this.prodottoOrigineId = prodottoOrigineId;
    }
}
