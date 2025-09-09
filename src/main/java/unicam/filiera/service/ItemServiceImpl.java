package unicam.filiera.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.*;
import unicam.filiera.model.StatoProdotto;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;

    public ItemServiceImpl(ProdottoService prodottoService,
                           PacchettoService pacchettoService,
                           ProdottoTrasformatoService trasformatoService) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
    }

    @Override
    public void creaItem(BaseItemDto dto, String creatore) {
        requireTipo(dto);
        switch (dto.getTipo()) {
            case PRODOTTO -> prodottoService.creaProdotto((ProdottoDto) dto, creatore);
            case PACCHETTO -> pacchettoService.creaPacchetto((PacchettoDto) dto, creatore);
            case TRASFORMATO -> trasformatoService.creaProdottoTrasformato((ProdottoTrasformatoDto) dto, creatore);
            default -> throw new IllegalArgumentException("Tipo item non supportato: " + dto.getTipo());
        }
    }

    @Override
    public void modificaRifiutato(BaseItemDto dto, String creatore) {
        requireTipo(dto);
        requireId(dto);
        switch (dto.getTipo()) {
            case PRODOTTO -> prodottoService.aggiornaProdotto(dto.getId(), (ProdottoDto) dto, creatore);
            case PACCHETTO -> pacchettoService.aggiornaPacchetto(dto.getId(), (PacchettoDto) dto, creatore);
            case TRASFORMATO -> trasformatoService.aggiornaProdottoTrasformato(dto.getId(), (ProdottoTrasformatoDto) dto, creatore);
            default -> throw new IllegalArgumentException("Tipo item non supportato: " + dto.getTipo());
        }
    }

    @Override
    public void eliminaById(Long id, ItemTipo tipo, String creatore) {
        if (id == null) throw new IllegalArgumentException("ID obbligatorio");
        if (tipo == null) throw new IllegalArgumentException("Tipo item obbligatorio");

        switch (tipo) {
            case PRODOTTO -> prodottoService.eliminaProdottoById(id, creatore);
            case PACCHETTO -> pacchettoService.eliminaPacchettoById(id, creatore);
            case TRASFORMATO -> trasformatoService.eliminaProdottoTrasformatoById(id, creatore);
            default -> throw new IllegalArgumentException("Tipo item non supportato: " + tipo);
        }
    }

    @Override
    public void cambiaStato(ItemTipo tipo, String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        if (tipo == null) throw new IllegalArgumentException("Tipo item obbligatorio");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome item obbligatorio");

        switch (tipo) {
            case PRODOTTO -> prodottoService.cambiaStatoProdotto(nome, creatore, nuovoStato, commento);
            case PACCHETTO -> pacchettoService.cambiaStatoPacchetto(nome, creatore, nuovoStato, commento);
            case TRASFORMATO -> trasformatoService.cambiaStatoProdottoTrasformato(nome, creatore, nuovoStato, commento);
            default -> throw new IllegalArgumentException("Tipo item non supportato: " + tipo);
        }
    }

    private void requireTipo(BaseItemDto dto) {
        if (dto == null || dto.getTipo() == null) {
            throw new IllegalArgumentException("Tipo item obbligatorio");
        }
    }

    private void requireId(BaseItemDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID obbligatorio per la modifica");
        }
    }
}
