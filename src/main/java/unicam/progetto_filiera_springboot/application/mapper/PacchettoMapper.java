package unicam.progetto_filiera_springboot.application.mapper;

import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.domain.model.Pacchetto;

public final class PacchettoMapper {
    private PacchettoMapper() {}

    public static PacchettoResponse toResponse(Pacchetto p) {
        PacchettoResponse r = new PacchettoResponse();
        r.setId(p.getId());
        r.setNome(p.getNome());
        r.setDescrizione(p.getDescrizione());
        r.setQuantita(p.getQuantita());
        r.setPrezzoTotale(p.getPrezzo());
        r.setIndirizzo(p.getIndirizzo());
        r.setCreatoDa(p.getCreatoDa().getUsername());
        r.setStato(p.getStato().name());
        r.setCommento(p.getCommento());
        r.setCreatedAt(p.getCreatedAt());

        // CSV come in ProdottoResponse
        r.setFoto(p.getFoto());
        r.setCertificati(p.getCertificati());

        // opzionale: elenco nomi dei prodotti inclusi
        r.setProdottiNomi(
                p.getProdotti().stream().map(prod -> prod.getNome()).toList()
        );

        return r;
    }
}
