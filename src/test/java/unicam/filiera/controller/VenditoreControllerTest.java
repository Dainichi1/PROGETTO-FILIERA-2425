package unicam.filiera.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.dto.*;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(VenditoreController.class)
class VenditoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdottoService prodottoService;

    @MockBean
    private PacchettoService pacchettoService;

    @MockBean
    private ProdottoTrasformatoService trasformatoService;

    // ---------------- FETCH ----------------

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Fetch ProdottoDto by ID - Success")
    void fetchProdotto_success() throws Exception {
        ProdottoDto prodotto = new ProdottoDto();
        prodotto.setId(1L);
        prodotto.setNome("Mela");
        prodotto.setDescrizione("Mela biologica");
        prodotto.setPrezzo(2.5);

        when(prodottoService.findDtoById(1L)).thenReturn(Optional.of(prodotto));

        mockMvc.perform(get("/venditore/item/fetch/1")
                        .param("tipo", "PRODOTTO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mela"))
                .andExpect(jsonPath("$.prezzo").value(2.5));
    }

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Fetch item not found - Returns 404")
    void fetchItem_notFound() throws Exception {
        when(prodottoService.findDtoById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/venditore/item/fetch/99")
                        .param("tipo", "PRODOTTO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------- MODIFICA ----------------

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Modifica ProdottoDto - Redirect con successo")
    void modificaProdotto_success() throws Exception {
        doNothing().when(prodottoService)
                .aggiornaProdotto(eq(1L), any(ProdottoDto.class), anyString());

        mockMvc.perform(post("/venditore/item/modifica")
                        .with(csrf())
                        .param("tipo", "PRODOTTO")
                        .param("id", "1")
                        .param("nome", "Mela aggiornata")
                        .param("descrizione", "Nuova descrizione")
                        .param("prezzo", "3.0")
                        .param("quantita", "10")     // aggiunto
                        .param("indirizzo", "Via Roma 1")) // aggiunto
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produttore/dashboard"));

        verify(prodottoService, times(1))
                .aggiornaProdotto(eq(1L), any(ProdottoDto.class), anyString());
    }

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Modifica ProdottoDto - Errore genera redirect / con errorMessage")
    void modificaProdotto_error() throws Exception {
        doThrow(new RuntimeException("Errore di test"))
                .when(prodottoService).aggiornaProdotto(eq(1L), any(ProdottoDto.class), anyString());

        mockMvc.perform(post("/venditore/item/modifica")
                        .with(csrf())
                        .param("tipo", "PRODOTTO")
                        .param("id", "1")
                        .param("nome", "Mela aggiornata")
                        .param("descrizione", "Nuova descrizione")
                        .param("prezzo", "3.0")
                        .param("quantita", "5")
                        .param("indirizzo", "Via Roma 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // ---------------- ELIMINA ----------------

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Elimina Prodotto - Successo")
    void eliminaProdotto_success() throws Exception {
        doNothing().when(prodottoService).eliminaById(1L, "demo_user");

        mockMvc.perform(delete("/venditore/item/elimina/1")
                        .with(csrf()) // aggiunto
                        .param("tipo", "PRODOTTO"))
                .andExpect(status().isOk())
                .andExpect(content().string("Elemento eliminato con successo"));

        verify(prodottoService, times(1)).eliminaById(1L, "demo_user");
    }

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Elimina Prodotto - Non autorizzato")
    void eliminaProdotto_forbidden() throws Exception {
        doThrow(new SecurityException("Non autorizzato"))
                .when(prodottoService).eliminaById(1L, "demo_user");

        mockMvc.perform(delete("/venditore/item/elimina/1")
                        .with(csrf()) // aggiunto
                        .param("tipo", "PRODOTTO"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Non autorizzato"));
    }

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Elimina Prodotto - Non trovato")
    void eliminaProdotto_notFound() throws Exception {
        doThrow(new IllegalArgumentException("Non trovato"))
                .when(prodottoService).eliminaById(1L, "demo_user");

        mockMvc.perform(delete("/venditore/item/elimina/1")
                        .with(csrf()) // aggiunto
                        .param("tipo", "PRODOTTO"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "demo_user")
    @DisplayName("Elimina Prodotto - Errore interno")
    void eliminaProdotto_internalError() throws Exception {
        doThrow(new RuntimeException("Errore interno"))
                .when(prodottoService).eliminaById(1L, "demo_user");

        mockMvc.perform(delete("/venditore/item/elimina/1")
                        .with(csrf()) // aggiunto
                        .param("tipo", "PRODOTTO"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Errore durante l'eliminazione: Errore interno"));
    }
}
