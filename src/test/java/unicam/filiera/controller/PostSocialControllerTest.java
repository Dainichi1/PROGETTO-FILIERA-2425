package unicam.filiera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.service.PostSocialService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostSocialController.class)
class PostSocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostSocialService service;

    @Autowired
    private ObjectMapper objectMapper;

    private PostSocialDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new PostSocialDto();
        validDto.setTitolo("Titolo valido");
        validDto.setTesto("Testo valido");
        validDto.setAutoreUsername("utente1");
        validDto.setNomeItem("ProdottoTest");
        validDto.setTipoItem("PRODOTTO");
        validDto.setCreatedAt(LocalDateTime.now());
    }

    // Caso OK
    @Test
    @WithMockUser(username = "utente1")
    void pubblicaPost_Ok() throws Exception {
        Mockito.when(service.pubblicaPost(anyLong(), eq("utente1"), any(PostSocialDto.class)))
                .thenReturn(validDto);

        mockMvc.perform(post("/api/social/pubblica/1")
                        .with(csrf()) // ✅ aggiunto
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titolo").value("Titolo valido"))
                .andExpect(jsonPath("$.autoreUsername").value("utente1"));
    }

    // Titolo mancante
    @Test
    @WithMockUser(username = "utente1")
    void pubblicaPost_TitoloMancante() throws Exception {
        PostSocialDto dto = new PostSocialDto();
        dto.setTitolo(""); // vuoto
        dto.setTesto("Testo valido");

        mockMvc.perform(post("/api/social/pubblica/1")
                        .with(csrf()) // ✅ aggiunto
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // Testo mancante
    @Test
    @WithMockUser(username = "utente1")
    void pubblicaPost_TestoMancante() throws Exception {
        PostSocialDto dto = new PostSocialDto();
        dto.setTitolo("Titolo valido");
        dto.setTesto(""); // vuoto

        mockMvc.perform(post("/api/social/pubblica/1")
                        .with(csrf()) // ✅ aggiunto
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // Item non trovato
    @Test
    @WithMockUser(username = "utente1")
    void pubblicaPost_ItemNonTrovato() throws Exception {
        Mockito.when(service.pubblicaPost(anyLong(), eq("utente1"), any(PostSocialDto.class)))
                .thenThrow(new IllegalArgumentException("Item non trovato"));

        mockMvc.perform(post("/api/social/pubblica/99")
                        .with(csrf()) // ✅ aggiunto
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Item non trovato"));
    }

    // GET tutti i post
    @Test
    @WithMockUser(username = "utente1")
    void getAllPosts() throws Exception {
        Mockito.when(service.getAllPosts()).thenReturn(List.of(validDto));

        mockMvc.perform(get("/api/social"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titolo").value("Titolo valido"));
    }
}
