package unicam.filiera;

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
import unicam.filiera.entity.PrenotazioneVisitaEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.PrenotazioneVisitaRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class PrenotazioneVisitaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepo;

    @Autowired
    private PrenotazioneVisitaRepository prenotazioneRepo;

    @BeforeEach
    void setup() {
        prenotazioneRepo.deleteAll();
        utenteRepo.deleteAll();

        UtenteEntity venditore = new UtenteEntity();
        venditore.setUsername("vend1");
        venditore.setPassword("pwd");
        venditore.setNome("Luca");
        venditore.setCognome("Bianchi");
        venditore.setRuolo(Ruolo.DISTRIBUTORE_TIPICITA); // esempio: un distributore invitato
        utenteRepo.save(venditore);
    }

    @Nested
    @DisplayName("CASI DI SUCCESSO")
    class SuccessCases {

        @Test
        @WithMockUser(username = "vend1", roles = {"DISTRIBUTORE"})
        @DisplayName("Prenotazione valida di una visita")
        void prenotazioneValida() throws Exception {
            mockMvc.perform(post("/prenotazioni-visite/prenota")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("idVisita", "1")
                            .param("numeroPersone", "3"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("✅ Prenotazione effettuata con successo!"));

            List<PrenotazioneVisitaEntity> prenotazioni = prenotazioneRepo.findAll();
            assertThat(prenotazioni).hasSize(1);
            assertThat(prenotazioni.get(0).getUsernameVenditore()).isEqualTo("vend1");
            assertThat(prenotazioni.get(0).getNumeroPersone()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("VALIDAZIONI FALLITE")
    class ValidationCases {

        @Test
        @WithMockUser(username = "vend1", roles = {"DISTRIBUTORE"})
        @DisplayName("Numero persone mancante o non valido")
        void numeroPersoneNonValido() throws Exception {
            mockMvc.perform(post("/prenotazioni-visite/prenota")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("idVisita", "1")
                            .param("numeroPersone", "0")) // non valido
                    .andExpect(status().isBadRequest()); // solo lo status, niente body

            assertThat(prenotazioneRepo.findAll()).isEmpty();
        }

        @Test
        @WithMockUser(username = "vend1", roles = {"DISTRIBUTORE"})
        @DisplayName("Prenotazione già esistente")
        void prenotazioneDuplicata() throws Exception {
            // prima prenotazione valida
            PrenotazioneVisitaEntity p = new PrenotazioneVisitaEntity();
            p.setIdVisita(2L);
            p.setUsernameVenditore("vend1");
            p.setNumeroPersone(2);
            prenotazioneRepo.save(p);

            // nuovo tentativo sullo stesso idVisita
            mockMvc.perform(post("/prenotazioni-visite/prenota")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("idVisita", "2")
                            .param("numeroPersone", "4"))
                    .andExpect(status().isBadRequest())   // invece di isConflict()
                    .andExpect(content().string("⚠ Hai già prenotato questa visita"));

            assertThat(prenotazioneRepo.findAll()).hasSize(1);
        }
    }
}
