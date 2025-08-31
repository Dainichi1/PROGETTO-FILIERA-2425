package unicam.filiera;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class DashboardControllerProduttoreTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository repo;

    @Test
    void quandoProduttoreEsiste_alloraMostraDashboardProduttore() throws Exception {
        UtenteEntity produttore = new UtenteEntity();
        produttore.setUsername("produttore1");
        produttore.setPassword("pass123");
        produttore.setNome("Luca");
        produttore.setCognome("Bianchi");
        produttore.setRuolo(Ruolo.PRODUTTORE);
        repo.save(produttore);

        mockMvc.perform(get("/dashboard/produttore1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/produttore"))
                .andExpect(model().attributeExists("utente"));

        UtenteEntity e = repo.findById("produttore1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.PRODUTTORE);
        assertThat(e.getNome()).isEqualTo("Luca");
    }

    @Test
    void quandoUtenteNonEsiste_alloraMostraErrore() throws Exception {
        mockMvc.perform(get("/dashboard/sconosciuto"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/utente_non_trovato"));
    }

    @Test
    void quandoAcquirenteEsiste_alloraMostraDashboardAcquirente() throws Exception {
        UtenteEntity acquirente = new UtenteEntity();
        acquirente.setUsername("acquirente1");
        acquirente.setPassword("pwd");
        acquirente.setNome("Giulia");
        acquirente.setCognome("Rossi");
        acquirente.setRuolo(Ruolo.ACQUIRENTE);
        repo.save(acquirente);

        mockMvc.perform(get("/dashboard/acquirente1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/acquirente"))
                .andExpect(model().attributeExists("utente"));

        UtenteEntity e = repo.findById("acquirente1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.ACQUIRENTE);
        assertThat(e.getNome()).isEqualTo("Giulia");
    }

    @Test
    void quandoUtenteHaRuoloNonGestito_alloraMostraDashboardGenerico() throws Exception {
        UtenteEntity animatore = new UtenteEntity();
        animatore.setUsername("animatore1");
        animatore.setPassword("pwd");
        animatore.setNome("Marco");
        animatore.setCognome("Verdi");
        animatore.setRuolo(Ruolo.ANIMATORE);
        repo.save(animatore);

        mockMvc.perform(get("/dashboard/animatore1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/generico"))
                .andExpect(model().attributeExists("utente"));

        UtenteEntity e = repo.findById("animatore1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.ANIMATORE);
        assertThat(e.getCognome()).isEqualTo("Verdi");
    }
}
