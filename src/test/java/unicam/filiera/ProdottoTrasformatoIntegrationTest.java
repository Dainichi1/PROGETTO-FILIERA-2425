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
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.OsservatoreProdottoTrasformato;
import unicam.filiera.repository.ProdottoTrasformatoRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdottoTrasformatoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UtenteRepository utenteRepo;
    @Autowired private ProdottoTrasformatoRepository ptRepo;
    @Autowired private SpyOsservatoreProdottoTrasformato spyObs;

    @TestConfiguration
    static class Config {
        @Bean @Primary
        SpyOsservatoreProdottoTrasformato spyObsBean() {
            return new SpyOsservatoreProdottoTrasformato();
        }
    }

    static class SpyOsservatoreProdottoTrasformato implements OsservatoreProdottoTrasformato {
        private final AtomicReference<ProdottoTrasformato> lastProd = new AtomicReference<>();
        private final AtomicReference<String> lastEvt = new AtomicReference<>();
        @Override
        public void notifica(ProdottoTrasformato p, String e) {
            lastProd.set(p);
            lastEvt.set(e);
        }
        public String getEvt() { return lastEvt.get(); }
        public ProdottoTrasformato getProd() { return lastProd.get(); }
    }

    @BeforeEach
    void setup() {
        ptRepo.deleteAll();
        utenteRepo.deleteAll();
        UtenteEntity tr = new UtenteEntity();
        tr.setUsername("tras1");
        tr.setPassword("pwd");
        tr.setNome("Luca");
        tr.setCognome("Bianchi");
        tr.setRuolo(Ruolo.TRASFORMATORE);
        utenteRepo.save(tr);

        spyObs.lastEvt.set(null);
        spyObs.lastProd.set(null);
    }

    @Test
    @WithMockUser(username="tras1", roles={"TRASFORMATORE"})
    void quandoCampiObbligatoriMancano_alloraErroriValidazione() throws Exception {
        mockMvc.perform(multipart("/trasformatore/crea")
                        .with(csrf())
                        .param("tipo", "TRASFORMATO") // importante!
                        .param("nome","")
                        .param("descrizione","")
                        .param("quantita","0")
                        .param("prezzo","-1")
                        .param("indirizzo","")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "prodottoTrasformatoDto",
                        "nome",
                        "descrizione",
                        "quantita",
                        "prezzo",
                        "indirizzo"
                ))
                .andExpect(model().attribute("showForm", true))
                .andExpect(view().name("dashboard/trasformatore"));

        assertThat(ptRepo.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username="tras1", roles={"TRASFORMATORE"})
    void quandoMancanoFasiProduzione_alloraErroreValidazione() throws Exception {
        mockMvc.perform(multipart("/trasformatore/crea")
                        .with(csrf())
                        .param("tipo", "TRASFORMATO")
                        .param("nome","Pane")
                        .param("descrizione","Pane fatto in casa")
                        .param("quantita","5")
                        .param("prezzo","1.5")
                        .param("indirizzo","Via Milano")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("prodottoTrasformatoDto"))
                .andExpect(model().attribute("showForm", true))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(view().name("dashboard/trasformatore"));

        assertThat(ptRepo.findAll()).isEmpty();
    }

    @Test
    @WithMockUser(username="tras1", roles={"TRASFORMATORE"})
    void quandoInputValido_alloraCreaEAvvisaOsservatore() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("certificati","cert.pdf","application/pdf","dummy".getBytes());
        MockMultipartFile foto = new MockMultipartFile("foto","fig.jpg","image/jpeg","dummy".getBytes());

        mockMvc.perform(multipart("/trasformatore/crea")
                        .file(cert).file(foto)
                        .with(csrf())
                        .param("tipo","TRASFORMATO")
                        .param("nome","Pane")
                        .param("descrizione","Pane fatto in casa")
                        .param("quantita","5")
                        .param("prezzo","1.5")
                        .param("indirizzo","Via Milano")
                        .param("fasiProduzione[0].descrizioneFase", "Impasto")
                        .param("fasiProduzione[0].produttoreUsername", "userX")
                        .param("fasiProduzione[0].prodottoOrigineId", "123")
                        .param("fasiProduzione[1].descrizioneFase", "Cottura")
                        .param("fasiProduzione[1].produttoreUsername", "userY")
                        .param("fasiProduzione[1].prodottoOrigineId", "456")
                )
                .andExpect(status().is3xxRedirection())  // ora passa
                .andExpect(redirectedUrl("/trasformatore/dashboard"))
                .andExpect(flash().attributeExists("createSuccessMessage"));

        assertThat(ptRepo.findAll()).hasSize(1);
        assertThat(ptRepo.findAll().get(0).getStato()).isEqualTo(StatoProdotto.IN_ATTESA);

        assertThat(spyObs.getEvt()).isEqualTo("NUOVO_PRODOTTO_TRASFORMATO");
        assertThat(spyObs.getProd()).isNotNull();
        assertThat(spyObs.getProd().getNome()).isEqualTo("Pane");
    }
}
