package unicam.filiera.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Acquirente;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Utente;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository repo;

    public CustomUserDetailsService(UtenteRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UtenteEntity entity = repo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        // Conversione verso il dominio
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
                .password("{noop}" + entity.getPassword()) // {noop} = nessun encoding, solo per test!
                .roles(entity.getRuolo().name())
                .build();
    }
}
