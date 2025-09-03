package unicam.filiera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class ProdottoCreazioneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepo;

    @Autowired
    private ProdottoRepository prodottoRepo;

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
    }

    @Test
    @WithMockUser(username = "prod1", roles = {"PRODUTTORE"})
    void quandoCampiObbligatoriMancano_alloraMostraErrori() throws Exception {
        mockMvc.perform(multipart("/produttore/crea")
                        .with(csrf())
                        .param("nome", "") // mancante
                        .param("descrizione", "") // mancante
                        .param("quantita", "0")   // non valido
                        .param("prezzo", "-5")    // non valido
                        .param("indirizzo", ""))  // mancante
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "prodottoDto",
                        "nome",
                        "descrizione",
                        "quantita",
                        "prezzo",
                        "indirizzo",
                        "certificati",
                        "foto"
                ))
                .andExpect(view().name("dashboard/produttore"));

        assertThat(prodottoRepo.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username = "prod1", roles = {"PRODUTTORE"})
    void quandoMancaUpload_alloraMostraErrori() throws Exception {
        mockMvc.perform(multipart("/produttore/crea")
                        .with(csrf())
                        .param("nome", "Mela")
                        .param("descrizione", "Mela rossa bio")
                        .param("quantita", "10")
                        .param("prezzo", "2.5")
                        .param("indirizzo", "Via Roma"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "prodottoDto",
                        "certificati",
                        "foto"
                ))
                .andExpect(view().name("dashboard/produttore"));

        assertThat(prodottoRepo.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username = "prod1", roles = {"PRODUTTORE"})
    void quandoDatiValidi_alloraCreaProdotto() throws Exception {
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
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("errorMessage"))
                .andExpect(model().attributeExists("successMessage"))
                .andExpect(view().name("dashboard/produttore"));

        List<ProdottoEntity> prodotti = prodottoRepo.findAll();
        assertThat(prodotti).hasSize(1);
        assertThat(prodotti.get(0).getNome()).isEqualTo("Mela");
        assertThat(prodotti.get(0).getStato()).isEqualTo(StatoProdotto.IN_ATTESA);
    }
}
