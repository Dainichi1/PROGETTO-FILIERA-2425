package unicam.filiera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties") // usa H2 in-memory per i test
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository repo;

    @BeforeEach
    void setup() {
        repo.deleteAll();

        // utente base: user / pass / PRODUTTORE
        UtenteEntity u = new UtenteEntity();
        u.setUsername("user");
        u.setPassword("pass");
        u.setNome("Mario");
        u.setCognome("Rossi");
        u.setRuolo(Ruolo.PRODUTTORE);
        u.setFondi(0.0);
        repo.save(u);
    }

    @Test
    void loginCorrettoConRuoloProduttore() throws Exception {
        mockMvc.perform(post("/doLogin")
                        .with(csrf()) // aggiunge CSRF token
                        .param("username", "user")
                        .param("password", "pass")
                        .param("ruolo", "PRODUTTORE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void loginConPasswordErrata() throws Exception {
        mockMvc.perform(post("/doLogin")
                        .with(csrf())
                        .param("username", "user")
                        .param("password", "wrong")
                        .param("ruolo", "PRODUTTORE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=credenziali"));
    }

    @Test
    void loginConRuoloSbagliato() throws Exception {
        mockMvc.perform(post("/doLogin")
                        .with(csrf())
                        .param("username", "user")
                        .param("password", "pass")
                        .param("ruolo", "ACQUIRENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=ruolo"));
    }

    @Test
    void loginSenzaRuolo() throws Exception {
        mockMvc.perform(post("/doLogin")
                        .with(csrf())
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=ruolo"));
    }
}
