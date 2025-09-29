package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.controller.RegistrazioneEsito;
import unicam.filiera.dto.UtenteDto;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {

    private final UtenteRepository repo;

    public UtenteService(UtenteRepository repo) {
        this.repo = repo;
    }

    public RegistrazioneEsito registraUtente(UtenteEntity u) {
        if (repo.existsByUsername(u.getUsername())) {
            return RegistrazioneEsito.USERNAME_GIA_ESISTENTE;
        }
        if (repo.existsByNomeAndCognome(u.getNome(), u.getCognome())) {
            return RegistrazioneEsito.PERSONA_GIA_REGISTRATA;
        }
        repo.save(u);
        return RegistrazioneEsito.SUCCESSO;
    }

    /**
     * Restituisce tutti gli utenti con ruolo PRODUTTORE (DTO).
     */
    public List<UtenteDto> getProduttori() {
        return repo.findByRuolo(Ruolo.PRODUTTORE)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Restituisce tutti i destinatari possibili per una visita (produttori, trasformatori, distributori tipicità).
     */
    public List<UtenteDto> getDestinatariPossibili() {
        return repo.findByRuoloIn(List.of(
                        Ruolo.PRODUTTORE,
                        Ruolo.TRASFORMATORE,
                        Ruolo.DISTRIBUTORE_TIPICITA
                ))
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Recupera un utente per username (DTO).
     */
    public Optional<UtenteDto> findByUsername(String username) {
        return repo.findByUsername(username)
                .map(this::mapToDto);
    }

    // ================= MAPPER =================
    private UtenteDto mapToDto(UtenteEntity e) {
        return UtenteDto.builder()
                .username(e.getUsername())
                .nome(e.getNome())
                .cognome(e.getCognome())
                .ruolo(e.getRuolo().name())
                .build();
    }
}
