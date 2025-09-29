package unicam.filiera.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.dto.FaseProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoPathDto;
import unicam.filiera.service.ProdottoTrasformatoPathService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdottoTrasformatoPathController.class)
class ProdottoTrasformatoPathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdottoTrasformatoPathService pathService;

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Get path prodotto trasformato - Successo")
    void getPath_success() throws Exception {
        FaseProdottoDto fase = new FaseProdottoDto(10L, "Mela", 43.7, 13.6, "Raccolta");
        ProdottoTrasformatoPathDto dto = new ProdottoTrasformatoPathDto(
                1L, "Succo di Mela", 43.6, 13.5, List.of(fase));

        when(pathService.getPath(1L)).thenReturn(dto);

        mockMvc.perform(get("/gestore/prodotti-trasformati/api/1/path")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trasformatoId").value(1))   // ✅ correggo qui
                .andExpect(jsonPath("$.nomeTrasformato").value("Succo di Mela"))
                .andExpect(jsonPath("$.fasi[0].nomeProdotto").value("Mela"));
    }

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Get path prodotto trasformato - Indirizzo non valido")
    void getPath_invalidAddress() throws Exception {
        when(pathService.getPath(2L))
                .thenThrow(new RuntimeException("Indirizzo non valido per prodotto trasformato"));

        mockMvc.perform(get("/gestore/prodotti-trasformati/api/2/path")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Errore interno: Indirizzo non valido per prodotto trasformato"));
    }
}
