package unicam.filiera.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import unicam.filiera.dto.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplModificaTest {

    private ProdottoService prodottoService;
    private PacchettoService pacchettoService;
    private ProdottoTrasformatoService trasformatoService;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setup() {
        prodottoService = mock(ProdottoService.class);
        pacchettoService = mock(PacchettoService.class);
        trasformatoService = mock(ProdottoTrasformatoService.class);
        itemService = new ItemServiceImpl(prodottoService, pacchettoService, trasformatoService);
    }

    @Test
    void modificaRifiutato_prodotto_callsProdottoService() {
        ProdottoDto dto = new ProdottoDto();
        dto.setId(1L);
        dto.setTipo(ItemTipo.PRODOTTO);

        itemService.modificaRifiutato(dto, "utente1");

        verify(prodottoService, times(1))
                .aggiornaProdotto(eq(1L), eq(dto), eq("utente1"));
        verifyNoInteractions(pacchettoService, trasformatoService);
    }

    @Test
    void modificaRifiutato_pacchetto_callsPacchettoService() {
        PacchettoDto dto = new PacchettoDto();
        dto.setId(2L);
        dto.setTipo(ItemTipo.PACCHETTO);

        itemService.modificaRifiutato(dto, "utente2");

        verify(pacchettoService, times(1))
                .aggiornaPacchetto(eq(2L), eq(dto), eq("utente2"));
        verifyNoInteractions(prodottoService, trasformatoService);
    }

    @Test
    void modificaRifiutato_trasformato_callsTrasformatoService() {
        ProdottoTrasformatoDto dto = new ProdottoTrasformatoDto();
        dto.setId(3L);
        dto.setTipo(ItemTipo.TRASFORMATO);

        itemService.modificaRifiutato(dto, "utente3");

        verify(trasformatoService, times(1))
                .aggiornaProdottoTrasformato(eq(3L), eq(dto), eq("utente3"));
        verifyNoInteractions(prodottoService, pacchettoService);
    }

    @Test
    void modificaRifiutato_senzaId_throws() {
        ProdottoDto dto = new ProdottoDto();
        dto.setTipo(ItemTipo.PRODOTTO); // ma id non settato

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> itemService.modificaRifiutato(dto, "utenteX"));

        assertEquals("ID obbligatorio per la modifica", ex.getMessage());
        verifyNoInteractions(prodottoService, pacchettoService, trasformatoService);
    }
}
