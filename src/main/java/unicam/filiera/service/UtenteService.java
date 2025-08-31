package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.controller.RegistrazioneEsito;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.repository.UtenteRepository;

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
}
