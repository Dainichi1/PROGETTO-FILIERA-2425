package unicam.filiera;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.PagamentoService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static unicam.filiera.dto.ItemTipo.PRODOTTO;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcquistoPagamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtenteRepository utenteRepo;

    @MockBean
    private PagamentoService pagamentoService;

    private DatiAcquistoDto acquistoValido;
    @Autowired
    private ProdottoRepository prodottoRepository;

    @BeforeEach
    void setup() {
        utenteRepo.deleteAll();

        // Item fittizio
        CartItemDto item = new CartItemDto();
        item.setId(1L);
        item.setTipo(PRODOTTO);
        item.setNome("Mela");
        item.setQuantita(2);
        item.setPrezzoUnitario(2.5);
        item.setTotale(5.0);

        acquistoValido = new DatiAcquistoDto();
        acquistoValido.setTotaleAcquisto(5.0);
        acquistoValido.setTipoMetodoPagamento(TipoMetodoPagamento.CARTA_DI_CREDITO);
        acquistoValido.setItems(List.of(item));
    }

    @Nested
    @DisplayName("CASI DI SUCCESSO")
    class SuccessCases {

        @Test
        @WithMockUser(username = "user1", roles = {"ACQUIRENTE"})
        @DisplayName("Pagamento alla consegna approvato con fondi sufficienti")
        void pagamentoAllaConsegnaSempreOK() throws Exception {
            // utente con fondi >= totaleAcquisto
            UtenteEntity acquirente = new UtenteEntity();
            acquirente.setUsername("user1");
            acquirente.setPassword("pwd");
            acquirente.setNome("Mario");
            acquirente.setCognome("Rossi");
            acquirente.setRuolo(Ruolo.ACQUIRENTE);
            acquirente.setFondi(50.0);
            utenteRepo.save(acquirente);

            // prodotto reale in DB
            ProdottoEntity prodotto = new ProdottoEntity();
            prodotto.setNome("Mela");
            prodotto.setDescrizione("Mela rossa biologica");
            prodotto.setIndirizzo("Via Roma 123, Ancona");
            prodotto.setPrezzo(2.5);
            prodotto.setQuantita(10);


            prodotto.setCreatoDa("produttore1");

            prodottoRepository.save(prodotto);


            acquistoValido.setTipoMetodoPagamento(TipoMetodoPagamento.PAGAMENTO_ALLA_CONSEGNA);
            Mockito.when(pagamentoService.effettuaPagamento(Mockito.any()))
                    .thenReturn(StatoPagamento.APPROVATO);

            mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.clearCart").value(false));
        }


        @Test
        @WithMockUser(username = "user1", roles = {"ACQUIRENTE"})
        @DisplayName("Pagamento con carta approvato con fondi sufficienti")
        void pagamentoCartaOK_fondiSufficienti() throws Exception {
            // utente con fondi sufficienti
            UtenteEntity acquirente = new UtenteEntity();
            acquirente.setUsername("user1");
            acquirente.setPassword("pwd");
            acquirente.setNome("Mario");
            acquirente.setCognome("Rossi");
            acquirente.setRuolo(Ruolo.ACQUIRENTE);
            acquirente.setFondi(100.0);
            utenteRepo.save(acquirente);

            acquistoValido.setTipoMetodoPagamento(TipoMetodoPagamento.CARTA_DI_CREDITO);
            Mockito.when(pagamentoService.effettuaPagamento(Mockito.any()))
                    .thenReturn(StatoPagamento.APPROVATO);

            var result = mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isOk())
                    .andReturn();

            Map resp = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
            assertThat(resp.get("success")).isEqualTo(true);
            assertThat(resp.get("clearCart")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("CASI DI ERRORE")
    class ErrorCases {

        @Test
        @WithMockUser(username = "user1", roles = {"ACQUIRENTE"})
        @DisplayName("Pagamento con carta rifiutato")
        void pagamentoCartaRifiutato() throws Exception {
            // utente con fondi sufficienti ma pagamento rifiutato dal servizio
            UtenteEntity acquirente = new UtenteEntity();
            acquirente.setUsername("user1");
            acquirente.setPassword("pwd");
            acquirente.setNome("Mario");
            acquirente.setCognome("Rossi");
            acquirente.setRuolo(Ruolo.ACQUIRENTE);
            acquirente.setFondi(100.0);
            utenteRepo.save(acquirente);

            acquistoValido.setTipoMetodoPagamento(TipoMetodoPagamento.CARTA_DI_CREDITO);
            Mockito.when(pagamentoService.effettuaPagamento(Mockito.any()))
                    .thenReturn(StatoPagamento.RIFIUTATO);

            var result = mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            Map resp = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
            assertThat(resp.get("success")).isEqualTo(false);
            assertThat(resp.get("clearCart")).isEqualTo(false);
        }

        @Test
        @WithMockUser(username = "user1", roles = {"ACQUIRENTE"})
        @DisplayName("Pagamento con carta approvato ma fondi insufficienti")
        void pagamentoCartaKO_fondiInsufficienti() throws Exception {
            // utente con fondi insufficienti
            UtenteEntity acquirente = new UtenteEntity();
            acquirente.setUsername("user1");
            acquirente.setPassword("pwd");
            acquirente.setNome("Mario");
            acquirente.setCognome("Rossi");
            acquirente.setRuolo(Ruolo.ACQUIRENTE);
            acquirente.setFondi(0.0);
            utenteRepo.save(acquirente);

            acquistoValido.setTipoMetodoPagamento(TipoMetodoPagamento.CARTA_DI_CREDITO);
            Mockito.when(pagamentoService.effettuaPagamento(Mockito.any()))
                    .thenReturn(StatoPagamento.APPROVATO); // pagamento ok ma fondi KO

            var result = mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            Map resp = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
            assertThat(resp.get("success")).isEqualTo(false);
            assertThat(resp.get("message")).isEqualTo("Fondi insufficienti");
        }

        @Test
        @WithMockUser(username = "user1", roles = {"ACQUIRENTE"})
        @DisplayName("Errore validazione DTO (nessun item)")
        void acquistoNonValido() throws Exception {
            acquistoValido.setItems(List.of()); // nessun item

            mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Utente non autenticato")
        void utenteNonAutenticato() throws Exception {
            mockMvc.perform(post("/acquirente/conferma-acquisto")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acquistoValido)))
                    .andExpect(status().isFound()) // 302 redirect
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }
}
