package unicam.filiera.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.model.StatoProdotto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ProdottoService prodottoService;
    private PacchettoService pacchettoService;
    private ProdottoTrasformatoService trasformatoService;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setup() {
        prodottoService = Mockito.mock(ProdottoService.class);
        pacchettoService = Mockito.mock(PacchettoService.class);
        trasformatoService = Mockito.mock(ProdottoTrasformatoService.class);

        itemService = new ItemServiceImpl(prodottoService, pacchettoService, trasformatoService);
    }

    @Test
    void eliminaNonApprovato_prodotto_ok() {
        Long id = 1L;
        String username = "user";

        assertDoesNotThrow(() -> itemService.eliminaNonApprovato(ItemTipo.PRODOTTO, id, username));
        verify(prodottoService, times(1)).eliminaById(id, username);
    }

    @Test
    void eliminaNonApprovato_pacchetto_ok() {
        Long id = 2L;
        String username = "user";

        assertDoesNotThrow(() -> itemService.eliminaNonApprovato(ItemTipo.PACCHETTO, id, username));
        verify(pacchettoService, times(1)).eliminaById(id, username);
    }

    @Test
    void eliminaNonApprovato_trasformato_ok() {
        Long id = 3L;
        String username = "user";

        assertDoesNotThrow(() -> itemService.eliminaNonApprovato(ItemTipo.TRASFORMATO, id, username));
        verify(trasformatoService, times(1)).eliminaById(id, username);
    }

    @Test
    void eliminaNonApprovato_tipoNull_throws() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> itemService.eliminaNonApprovato(null, 1L, "user"));
        assertEquals("Tipo item obbligatorio", ex.getMessage());
    }

    @Test
    void eliminaNonApprovato_idNull_throws() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> itemService.eliminaNonApprovato(ItemTipo.PRODOTTO, null, "user"));
        assertEquals("ID obbligatorio", ex.getMessage());
    }

}
