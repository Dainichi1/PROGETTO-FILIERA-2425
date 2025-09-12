package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.controller.RegistrazioneEsito;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;

import java.util.List;

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
     * Restituisce tutti gli utenti con ruolo PRODUTTORE.
     */
    public List<UtenteEntity> getProduttori() {
        return repo.findByRuolo(Ruolo.PRODUTTORE);
    }

    /**
     * Restituisce tutti i destinatari possibili per una visita (produttori, trasformatori, distributori tipicità).
     */
    public List<UtenteEntity> getDestinatariPossibili() {
        return repo.findByRuoloIn(List.of(
                Ruolo.PRODUTTORE,
                Ruolo.TRASFORMATORE,
                Ruolo.DISTRIBUTORE_TIPICITA
        ));
    }
}
