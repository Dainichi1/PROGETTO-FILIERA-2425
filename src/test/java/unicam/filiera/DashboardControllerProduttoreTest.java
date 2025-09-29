package unicam.filiera;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
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
    @WithMockUser(username = "produttore1", roles = {"PRODUTTORE"})
    void quandoProduttoreEsiste_alloraRedirectDashboardProduttore() throws Exception {
        UtenteEntity produttore = new UtenteEntity();
        produttore.setUsername("produttore1");
        produttore.setPassword("pass123");
        produttore.setNome("Luca");
        produttore.setCognome("Bianchi");
        produttore.setRuolo(Ruolo.PRODUTTORE);
        repo.save(produttore);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produttore/dashboard"));

        UtenteEntity e = repo.findById("produttore1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.PRODUTTORE);
        assertThat(e.getNome()).isEqualTo("Luca");
    }

    @Test
    @WithMockUser(username = "sconosciuto", roles = {"PRODUTTORE"})
    void quandoUtenteNonEsiste_alloraMostraErrore() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/utente_non_trovato"));
    }

    @Test
    @WithMockUser(username = "acquirente1", roles = {"ACQUIRENTE"})
    void quandoAcquirenteEsiste_alloraRedirectDashboardAcquirente() throws Exception {
        UtenteEntity acquirente = new UtenteEntity();
        acquirente.setUsername("acquirente1");
        acquirente.setPassword("pwd");
        acquirente.setNome("Giulia");
        acquirente.setCognome("Rossi");
        acquirente.setRuolo(Ruolo.ACQUIRENTE);
        repo.save(acquirente);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acquirente/dashboard"));

        UtenteEntity e = repo.findById("acquirente1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.ACQUIRENTE);
        assertThat(e.getNome()).isEqualTo("Giulia");
    }

    @Test
    @WithMockUser(username = "animatore1", roles = {"ANIMATORE"})
    void quandoAnimatoreEsiste_alloraRedirectDashboardAnimatore() throws Exception {
        UtenteEntity animatore = new UtenteEntity();
        animatore.setUsername("animatore1");
        animatore.setPassword("pwd");
        animatore.setNome("Marco");
        animatore.setCognome("Verdi");
        animatore.setRuolo(Ruolo.ANIMATORE);
        repo.save(animatore);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animatore/dashboard"));

        UtenteEntity e = repo.findById("animatore1").orElseThrow();
        assertThat(e.getRuolo()).isEqualTo(Ruolo.ANIMATORE);
        assertThat(e.getCognome()).isEqualTo("Verdi");
    }
}
