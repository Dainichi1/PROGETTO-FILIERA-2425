package unicam.filiera.factory;

import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.entity.AcquistoEntity;
import unicam.filiera.entity.PostSocialEntity;
import unicam.filiera.model.Evento;
import unicam.filiera.model.Item;

public final class PostSocialFactory {

    private PostSocialFactory() { }

    /** Post da Item (Prodotto, Pacchetto, Trasformato) */
    public static PostSocialEntity creaPost(PostSocialDto dto, Item item, String autoreUsername) {
        return PostSocialEntity.builder()
                .autoreUsername(autoreUsername)
                .nomeItem(item.getNome())
                .tipoItem(item.getTipo().name()) // PRODOTTO | PACCHETTO | TRASFORMATO
                .titolo(dto.getTitolo())
                .testo(dto.getTesto())
                .build();
    }

    /** Post da Evento (Fiera, Visita) */
    public static PostSocialEntity creaPost(PostSocialDto dto, Evento evento, String autoreUsername) {
        return PostSocialEntity.builder()
                .autoreUsername(autoreUsername)
                .nomeItem(evento.getNome())
                .tipoItem(evento.getTipo().name()) // FIERA | VISITA
                .titolo(dto.getTitolo())
                .testo(dto.getTesto())
                .build();
    }

    public static PostSocialEntity creaRecensione(PostSocialDto dto,
                                                  AcquistoEntity acquisto,
                                                  String autoreUsername) {
        return PostSocialEntity.builder()
                .autoreUsername(autoreUsername)
                .idAcquisto(acquisto.getId().intValue())
                .nomeItem("Acquisto #" + acquisto.getId())
                .tipoItem("ACQUISTO")
                .titolo(dto.getTitolo())
                .testo(dto.getTesto())
                .build();
    }

}
