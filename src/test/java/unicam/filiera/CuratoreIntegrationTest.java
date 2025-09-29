package unicam.filiera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.ProdottoNotifier;
import unicam.filiera.repository.ProdottoRepository;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CuratoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdottoRepository prodottoRepository;

    @Autowired
    private SpyNotifier spyNotifier;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        SpyNotifier spyNotifier() {
            return new SpyNotifier();
        }
    }

    static class SpyNotifier extends ProdottoNotifier {
        private final AtomicReference<String> lastEvento = new AtomicReference<>();
        private final AtomicReference<Prodotto> lastProdotto = new AtomicReference<>();

        public SpyNotifier() {
            super(java.util.List.of()); // nessun osservatore reale
        }

        @Override
        public void notificaTutti(Prodotto prodotto, String evento) {
            lastProdotto.set(prodotto);
            lastEvento.set(evento);
        }

        public String getLastEvento() {
            return lastEvento.get();
        }

        public Prodotto getLastProdotto() {
            return lastProdotto.get();
        }

        public void reset() {
            lastEvento.set(null);
            lastProdotto.set(null);
        }
    }

    @BeforeEach
    void resetSpy() {
        spyNotifier.reset();
        prodottoRepository.deleteAll();
    }

    private ProdottoEntity creaProdottoInAttesa() {
        ProdottoEntity p = new ProdottoEntity();
        p.setNome("MelaTest");
        p.setDescrizione("Mela rossa di test");
        p.setIndirizzo("Via Roma 123");
        p.setQuantita(10);
        p.setPrezzo(5.5);
        p.setCreatoDa("produttoreTest");
        p.setStato(StatoProdotto.IN_ATTESA);
        return prodottoRepository.save(p);
    }

    @Test
    @WithMockUser(username = "curatore1", roles = {"CURATORE"})
    void approvaProdotto_cambiaStatoInApprovato_eNotifica() throws Exception {
        ProdottoEntity prodotto = creaProdottoInAttesa();

        mockMvc.perform(post("/curatore/approvaProdotto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", prodotto.getNome())
                        .param("creatore", prodotto.getCreatoDa()))
                .andExpect(status().is3xxRedirection());

        ProdottoEntity aggiornato = prodottoRepository
                .findByNomeAndCreatoDa(prodotto.getNome(), prodotto.getCreatoDa())
                .orElseThrow();

        assertThat(aggiornato.getStato()).isEqualTo(StatoProdotto.APPROVATO);
        assertThat(spyNotifier.getLastEvento()).isEqualTo("APPROVATO");
        assertThat(spyNotifier.getLastProdotto()).isNotNull();
        assertThat(spyNotifier.getLastProdotto().getNome()).isEqualTo("MelaTest");
    }

    @Test
    @WithMockUser(username = "curatore1", roles = {"CURATORE"})
    void rifiutaProdotto_cambiaStatoInRifiutato_eSalvaCommento_eNotifica() throws Exception {
        ProdottoEntity prodotto = creaProdottoInAttesa();

        mockMvc.perform(post("/curatore/rifiutaProdotto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", prodotto.getNome())
                        .param("creatore", prodotto.getCreatoDa())
                        .param("commento", "Non conforme"))
                .andExpect(status().is3xxRedirection());

        ProdottoEntity aggiornato = prodottoRepository
                .findByNomeAndCreatoDa(prodotto.getNome(), prodotto.getCreatoDa())
                .orElseThrow();

        assertThat(aggiornato.getStato()).isEqualTo(StatoProdotto.RIFIUTATO);
        assertThat(aggiornato.getCommento()).isEqualTo("Non conforme");
        assertThat(spyNotifier.getLastEvento()).isEqualTo("RIFIUTATO");
    }

    @Test
    @WithMockUser(username = "curatore1", roles = {"CURATORE"})
    void rifiutaProdotto_senzaCommento_funzione() throws Exception {
        ProdottoEntity prodotto = creaProdottoInAttesa();

        mockMvc.perform(post("/curatore/rifiutaProdotto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", prodotto.getNome())
                        .param("creatore", prodotto.getCreatoDa()))
                .andExpect(status().is3xxRedirection());

        ProdottoEntity aggiornato = prodottoRepository
                .findByNomeAndCreatoDa(prodotto.getNome(), prodotto.getCreatoDa())
                .orElseThrow();

        assertThat(aggiornato.getStato()).isEqualTo(StatoProdotto.RIFIUTATO);
        assertThat(aggiornato.getCommento()).isNull();
        assertThat(spyNotifier.getLastEvento()).isEqualTo("RIFIUTATO");
    }

    @Test
    @WithMockUser(username = "curatore1", roles = {"CURATORE"})
    void approvaProdotto_nonEsistente_nonCambiaStato_eNessunaNotifica() throws Exception {
        mockMvc.perform(post("/curatore/approvaProdotto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Inesistente")
                        .param("creatore", "sconosciuto"))
                .andExpect(status().is3xxRedirection());

        assertThat(prodottoRepository.findByNomeAndCreatoDa("Inesistente", "sconosciuto")).isEmpty();
        assertThat(spyNotifier.getLastEvento()).isNull();
    }

    @Test
    @WithMockUser(username = "curatore1", roles = {"CURATORE"})
    void rifiutaProdotto_nonEsistente_nonCambiaStato_eNessunaNotifica() throws Exception {
        mockMvc.perform(post("/curatore/rifiutaProdotto")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Inesistente")
                        .param("creatore", "sconosciuto"))
                .andExpect(status().is3xxRedirection());

        assertThat(prodottoRepository.findByNomeAndCreatoDa("Inesistente", "sconosciuto")).isEmpty();
        assertThat(spyNotifier.getLastEvento()).isNull();
    }
}
