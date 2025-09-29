package unicam.filiera;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FieraCreationTests {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("✅ CASI DI SUCCESSO")
    class SuccessCases {

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Crea fiera valida")
        void creaFieraValida() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "Fiera Agricola")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "Roma")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(12).toString())
                            .param("prezzo", "25.0"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/animatore/dashboard"));
        }
    }

    @Nested
    @DisplayName("❌ VALIDAZIONI FALLITE")
    class ValidationCases {

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Nome mancante")
        void nomeMancante() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "Roma")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(12).toString())
                            .param("prezzo", "25.0"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("fieraDto", "nome"))
                    .andExpect(view().name("dashboard/animatore"));
        }

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Prezzo mancante → errore gestito dal service")
        void prezzoMancante() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "Fiera")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "Roma")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(12).toString()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"))
                    .andExpect(view().name("dashboard/animatore"));
        }

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Prezzo negativo")
        void prezzoNegativo() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "Fiera")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "Roma")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(12).toString())
                            .param("prezzo", "-10.0"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("fieraDto", "prezzo"))
                    .andExpect(view().name("dashboard/animatore"));
        }

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Data fine prima della data inizio → errore gestito dal service")
        void dateNonValide() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "Fiera")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "Roma")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(5).toString())
                            .param("prezzo", "30.0"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"))
                    .andExpect(view().name("dashboard/animatore"));
        }

        @Test
        @WithMockUser(username = "animatore", roles = {"ANIMATORE"})
        @DisplayName("Indirizzo mancante")
        void indirizzoMancante() throws Exception {
            mockMvc.perform(post("/animatore/crea-fiera")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("tipo", "FIERA")
                            .param("nome", "Fiera")
                            .param("descrizione", "Descrizione evento")
                            .param("indirizzo", "")
                            .param("dataInizio", LocalDate.now().plusDays(10).toString())
                            .param("dataFine", LocalDate.now().plusDays(12).toString())
                            .param("prezzo", "50.0"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("fieraDto", "indirizzo"))
                    .andExpect(view().name("dashboard/animatore"));
        }
    }
}
