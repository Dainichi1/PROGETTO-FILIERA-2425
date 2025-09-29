package unicam.filiera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.UtenteRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class DistributorePacchettoEliminazioneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacchettoRepository pacchettoRepo;

    @Autowired
    private ProdottoRepository prodottoRepo;

    @Autowired
    private UtenteRepository utenteRepo;

    private Long idPacchettoEliminabile;
    private Long idPacchettoApprovato;

    @BeforeEach
    void setup() {
        pacchettoRepo.deleteAll();
        prodottoRepo.deleteAll();
        utenteRepo.deleteAll();

        // utente distributore
        UtenteEntity distributore = new UtenteEntity();
        distributore.setUsername("dist1");
        distributore.setPassword("pwd");
        distributore.setNome("Luca");
        distributore.setCognome("Bianchi");
        distributore.setRuolo(Ruolo.DISTRIBUTORE_TIPICITA);
        utenteRepo.save(distributore);

        // prodotti fittizi
        ProdottoEntity prod1 = new ProdottoEntity();
        prod1.setNome("Prodotto1");
        prod1.setDescrizione("desc1");
        prod1.setQuantita(10);
        prod1.setPrezzo(5.0);
        prod1.setIndirizzo("Via A");
        prod1.setCreatoDa("dist1");
        prod1.setStato(StatoProdotto.APPROVATO);

        ProdottoEntity prod2 = new ProdottoEntity();
        prod2.setNome("Prodotto2");
        prod2.setDescrizione("desc2");
        prod2.setQuantita(8);
        prod2.setPrezzo(7.5);
        prod2.setIndirizzo("Via B");
        prod2.setCreatoDa("dist1");
        prod2.setStato(StatoProdotto.APPROVATO);

        prodottoRepo.save(prod1);
        prodottoRepo.save(prod2);

        // pacchetto eliminabile (IN_ATTESA con 2 prodotti)
        PacchettoEntity p1 = new PacchettoEntity();
        p1.setNome("Pacchetto eliminabile");
        p1.setDescrizione("Descrizione");
        p1.setQuantita(5);
        p1.setPrezzo(10.0);
        p1.setIndirizzo("Via Roma");
        p1.setCreatoDa("dist1");
        p1.setStato(StatoProdotto.IN_ATTESA);
        p1.setProdotti(Set.of(prod1, prod2));
        pacchettoRepo.save(p1);
        idPacchettoEliminabile = p1.getId();

        // pacchetto NON eliminabile (APPROVATO con 2 prodotti)
        PacchettoEntity p2 = new PacchettoEntity();
        p2.setNome("Pacchetto approvato");
        p2.setDescrizione("Descrizione");
        p2.setQuantita(3);
        p2.setPrezzo(20.0);
        p2.setIndirizzo("Via Milano");
        p2.setCreatoDa("dist1");
        p2.setStato(StatoProdotto.APPROVATO);
        p2.setProdotti(Set.of(prod1, prod2));
        pacchettoRepo.save(p2);
        idPacchettoApprovato = p2.getId();
    }

    @Test
    @WithMockUser(username = "dist1", roles = {"DISTRIBUTORE_TIPICITA"})
    void quandoPacchettoNonEsiste_allora404() throws Exception {
        mockMvc.perform(delete("/venditore/item/elimina/9999")
                        .param("tipo", "PACCHETTO")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "dist1", roles = {"DISTRIBUTORE_TIPICITA"})
    void quandoPacchettoNonEliminabile_allora400() throws Exception {
        mockMvc.perform(delete("/venditore/item/elimina/" + idPacchettoApprovato)
                        .param("tipo", "PACCHETTO")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        assertThat(pacchettoRepo.findById(idPacchettoApprovato)).isPresent();
    }

    @Test
    @WithMockUser(username = "dist1", roles = {"DISTRIBUTORE_TIPICITA"})
    void quandoPacchettoEliminabile_allora200eRimosso() throws Exception {
        mockMvc.perform(delete("/venditore/item/elimina/" + idPacchettoEliminabile)
                        .param("tipo", "PACCHETTO")
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(pacchettoRepo.findById(idPacchettoEliminabile)).isEmpty();
    }
}
