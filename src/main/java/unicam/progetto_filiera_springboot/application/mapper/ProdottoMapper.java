package unicam.progetto_filiera_springboot.application.mapper;

import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import java.util.Objects;

public final class ProdottoMapper {
    private ProdottoMapper() {}

    public static ProdottoResponse toResponse(Prodotto p) {
        ProdottoResponse r = new ProdottoResponse();
        r.setId(p.getId());
        r.setNome(p.getNome());
        r.setDescrizione(p.getDescrizione());
        r.setQuantita(p.getQuantita());
        r.setPrezzo(p.getPrezzo());
        r.setIndirizzo(p.getIndirizzo());

        if (p.getCreatoDa() != null) {
            r.setCreatoDa(p.getCreatoDa().getUsername());
        }
        if (p.getStato() != null) {
            r.setStato(p.getStato().name());
        }

        r.setCommento(p.getCommento());
        r.setCreatedAt(p.getCreatedAt());
        r.setCertificati(Objects.toString(p.getCertificati(), ""));
        r.setFoto(Objects.toString(p.getFoto(), ""));

        return r;
    }
}
