package unicam.filiera;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class CarrelloAggiuntaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdottoRepository prodottoRepo;

    @Autowired
    private UtenteRepository utenteRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private ProdottoEntity prodottoDisponibile;

    @BeforeEach
    void setup() {
        prodottoRepo.deleteAll();
        utenteRepo.deleteAll();

        UtenteEntity produttore = new UtenteEntity();
        produttore.setUsername("prod1");
        produttore.setPassword("pwd");
        produttore.setNome("Mario");
        produttore.setCognome("Rossi");
        produttore.setRuolo(Ruolo.PRODUTTORE);
        utenteRepo.save(produttore);

        // creo un prodotto con disponibilità 10
        prodottoDisponibile = new ProdottoEntity();
        prodottoDisponibile.setNome("Mela");
        prodottoDisponibile.setDescrizione("Mela rossa");
        prodottoDisponibile.setQuantita(10);
        prodottoDisponibile.setPrezzo(2.5);
        prodottoDisponibile.setIndirizzo("Via Roma");
        prodottoDisponibile.setStato(StatoProdotto.APPROVATO);
        prodottoDisponibile.setCreatoDa("prod1");
        prodottoRepo.save(prodottoDisponibile);
    }

    @Nested
    @DisplayName("CASI DI SUCCESSO")
    class SuccessCases {

        @Test
        @WithMockUser(username = "acq1", roles = {"ACQUIRENTE"})
        @DisplayName("Aggiunta valida di un prodotto al carrello")
        void aggiuntaValida() throws Exception {
            Map<String, Object> payload = Map.of(
                    "id", prodottoDisponibile.getId(),
                    "tipo", ItemTipo.PRODOTTO.name(),
                    "quantita", 3
            );

            mockMvc.perform(post("/carrello/aggiungi")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.items[0].nome").value("Mela"))
                    .andExpect(jsonPath("$.items[0].quantita").value(3))
                    .andExpect(jsonPath("$.totali.totaleArticoli").value(3));
        }

        @Test
        @WithMockUser(username = "acq1", roles = {"ACQUIRENTE"})
        @DisplayName("Aggiunta multipla somma le quantità")
        void aggiuntaMultipla() throws Exception {
            Map<String, Object> payload = Map.of(
                    "id", prodottoDisponibile.getId(),
                    "tipo", ItemTipo.PRODOTTO.name(),
                    "quantita", 2
            );

            // creo una sessione mock
            var session = new org.springframework.mock.web.MockHttpSession();

            // prima aggiunta con la stessa sessione
            mockMvc.perform(post("/carrello/aggiungi")
                            .session(session)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk());

            // seconda aggiunta con la stessa sessione
            mockMvc.perform(post("/carrello/aggiungi")
                            .session(session)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantita").value(4))
                    .andExpect(jsonPath("$.totali.totaleArticoli").value(4));
        }
    }

    @Nested
    @DisplayName("VALIDAZIONI FALLITE")
    class ValidationCases {

        @Test
        @WithMockUser(username = "acq1", roles = {"ACQUIRENTE"})
        @DisplayName("Quantità negativa o zero non valida")
        void quantitaNonValida() throws Exception {
            Map<String, Object> payload = Map.of(
                    "id", prodottoDisponibile.getId(),
                    "tipo", ItemTipo.PRODOTTO.name(),
                    "quantita", 0
            );

            mockMvc.perform(post("/carrello/aggiungi")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        @WithMockUser(username = "acq1", roles = {"ACQUIRENTE"})
        @DisplayName("Quantità richiesta superiore alla disponibilità")
        void quantitaSuperioreDisponibilita() throws Exception {
            Map<String, Object> payload = Map.of(
                    "id", prodottoDisponibile.getId(),
                    "tipo", ItemTipo.PRODOTTO.name(),
                    "quantita", 20 // > 10 disponibili
            );

            mockMvc.perform(post("/carrello/aggiungi")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("⚠ Quantità richiesta superiore alla disponibilità (10)"));
        }

        @Test
        @WithMockUser(username = "acq1", roles = {"ACQUIRENTE"})
        @DisplayName("Tipo item mancante o errato")
        void tipoMancante() throws Exception {
            Map<String, Object> payload = Map.of(
                    "id", prodottoDisponibile.getId(),
                    "quantita", 1
            );

            mockMvc.perform(post("/carrello/aggiungi")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }
    }
}
