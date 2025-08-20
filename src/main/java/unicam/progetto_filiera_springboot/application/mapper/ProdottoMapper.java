package unicam.progetto_filiera_springboot.application.mapper;

import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;

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
        r.setCreatoDa(p.getCreatoDa().getUsername());
        r.setStato(p.getStato().name());
        r.setCommento(p.getCommento());
        r.setCreatedAt(p.getCreatedAt());
        r.setCertificati(p.getCertificati());
        r.setFoto(p.getFoto());
        return r;
    }
}
