package unicam.filiera.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.dto.MarkerDto;
import unicam.filiera.service.MarkerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // ✅ qui ci sono post(), get(), delete(), etc.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // ✅ status(), jsonPath(), content()
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // ✅ per csrf()


@WebMvcTest(MarkerController.class)
class MarkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarkerService markerService; // mockiamo direttamente il service, non il repository

    // ========== SINGOLO MARKER ==========

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Salvataggio singolo marker - Successo")
    void saveMarker_success() throws Exception {
        MarkerDto saved = new MarkerDto(1L, 43.6, 13.5, "Fiera Macerata", "red");

        when(markerService.saveMarker(any())).thenReturn(saved);

        mockMvc.perform(post("/gestore/markers/api")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"lat":43.6,"lng":13.5,"label":"Fiera Macerata","color":"red"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.label").value("Fiera Macerata"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Salvataggio singolo marker - Duplicato")
    void saveMarker_duplicate() throws Exception {
        MarkerDto existing = new MarkerDto(99L, 40.0, 10.0, "Fiera Macerata", "red");

        when(markerService.saveMarker(any())).thenReturn(existing);

        mockMvc.perform(post("/gestore/markers/api")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"lat":43.6,"lng":13.5,"label":"Fiera Macerata","color":"red"}
                            """))
                .andExpect(status().isOk()) // ritorna quello esistente
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.label").value("Fiera Macerata"));
    }

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Salvataggio singolo marker - Indirizzo non valido (eccezione)")
    void saveMarker_invalidAddress() throws Exception {
        when(markerService.saveMarker(any()))
                .thenThrow(new IllegalArgumentException("Indirizzo non valido"));

        mockMvc.perform(post("/gestore/markers/api")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"lat":null,"lng":null,"label":"Azienda Fantasma","color":"blue"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Indirizzo non valido"));
    }

    // ========== MULTIPLI MARKER ==========

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Salvataggio multiplo marker - Successo")
    void saveMarkers_success() throws Exception {
        MarkerDto marker1 = new MarkerDto(1L, 43.6, 13.5, "Fiera", "red");
        MarkerDto marker2 = new MarkerDto(2L, 43.7, 13.6, "Azienda", "blue");

        when(markerService.saveMarkers(anyList())).thenReturn(List.of(marker1, marker2));

        mockMvc.perform(post("/gestore/markers/api/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            [
                              {"lat":43.6,"lng":13.5,"label":"Fiera","color":"red"},
                              {"lat":43.7,"lng":13.6,"label":"Azienda","color":"blue"}
                            ]
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "gestore")
    @DisplayName("Salvataggio multiplo marker - Uno degli indirizzi non valido")
    void saveMarkers_invalidAddress() throws Exception {
        when(markerService.saveMarkers(anyList()))
                .thenThrow(new IllegalArgumentException("Indirizzo non valido in uno degli elementi"));

        mockMvc.perform(post("/gestore/markers/api/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        [
                          {"lat":43.6,"lng":13.5,"label":"Fiera","color":"red"},
                          {"lat":null,"lng":null,"label":"Indirizzo sbagliato","color":"blue"}
                        ]
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Indirizzo non valido in uno degli elementi"));
    }
}