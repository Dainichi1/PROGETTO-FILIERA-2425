package unicam.filiera.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.model.Acquirente;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Utente;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.repository.RichiestaEliminazioneProfiloRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository repo;
    private final RichiestaEliminazioneProfiloRepository richiestaRepo;

    public CustomUserDetailsService(UtenteRepository repo,
                                    RichiestaEliminazioneProfiloRepository richiestaRepo) {
        this.repo = repo;
        this.richiestaRepo = richiestaRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UtenteEntity entity = repo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        // Se esiste una richiesta APPROVATA → blocco login
        boolean eliminato = richiestaRepo.findByUsername(entity.getUsername())
                .stream()
                .anyMatch(r -> r.getStato() == StatoRichiestaEliminazioneProfilo.APPROVATA);

        if (eliminato) {
            throw new UsernameNotFoundException("Utente non trovato"); // → mostra "credenziali non valide"
        }

        // Conversione verso il dominio (se serve in futuro)
        Utente utenteDomain;
        if (entity.getRuolo() == Ruolo.ACQUIRENTE) {
            utenteDomain = new Acquirente(
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getNome(),
                    entity.getCognome(),
                    entity.getFondi() != null ? entity.getFondi() : 0.0
            );
        } else {
            utenteDomain = new UtenteAutenticato(
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getNome(),
                    entity.getCognome(),
                    entity.getRuolo()
            );
        }

        // Mappa verso UserDetails (Spring Security)
        return User.withUsername(entity.getUsername())
                .password("{noop}" + entity.getPassword()) // {noop} = nessun encoding, solo test
                .roles(entity.getRuolo().name())
                .build();
    }
}
