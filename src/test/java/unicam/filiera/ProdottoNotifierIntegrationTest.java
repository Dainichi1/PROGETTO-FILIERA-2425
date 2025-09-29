package unicam.filiera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Ruolo;
import unicam.filiera.observer.OsservatoreProdotto;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdottoNotifierIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepo;

    @Autowired
    private ProdottoRepository prodottoRepo;

    @Autowired
    private SpyOsservatoreProdotto spyOsservatore;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        SpyOsservatoreProdotto spyOsservatoreProdotto() {
            return new SpyOsservatoreProdotto();
        }
    }

    static class SpyOsservatoreProdotto implements OsservatoreProdotto {
        private final AtomicReference<String> lastEvento = new AtomicReference<>();
        private final AtomicReference<Prodotto> lastProdotto = new AtomicReference<>();

        @Override
        public void notifica(Prodotto prodotto, String evento) {
            lastProdotto.set(prodotto);
            lastEvento.set(evento);
        }

        public String getLastEvento() {
            return lastEvento.get();
        }

        public Prodotto getLastProdotto() {
            return lastProdotto.get();
        }
    }

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

        spyOsservatore.lastEvento.set(null);
        spyOsservatore.lastProdotto.set(null);
    }

    @Test
    @WithMockUser(username = "prod1", roles = {"PRODUTTORE"})
    void quandoProdottoCreato_alloraNotifierInformaOsservatore() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("certificati", "cert.pdf", "application/pdf", "fake".getBytes());
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "fake".getBytes());

        mockMvc.perform(multipart("/produttore/crea")
                        .file(cert)
                        .file(foto)
                        .with(csrf())
                        .param("nome", "Mela")
                        .param("descrizione", "Mela rossa bio")
                        .param("quantita", "10")
                        .param("prezzo", "2.5")
                        .param("indirizzo", "Via Roma"))
                .andExpect(status().is3xxRedirection())                // <-- invece di isOk()
                .andExpect(redirectedUrl("/produttore/dashboard"))     // <-- verifica redirect
                .andExpect(flash().attribute("createSuccessMessage",
                        "Prodotto inviato al Curatore con successo"));

        // Verifica che l’osservatore sia stato notificato
        assertThat(spyOsservatore.getLastEvento()).isEqualTo("NUOVO_PRODOTTO");
        assertThat(spyOsservatore.getLastProdotto()).isNotNull();
        assertThat(spyOsservatore.getLastProdotto().getNome()).isEqualTo("Mela");
    }
}
