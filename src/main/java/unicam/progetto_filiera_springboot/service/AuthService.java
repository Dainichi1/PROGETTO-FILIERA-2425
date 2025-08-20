package unicam.progetto_filiera_springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unicam.progetto_filiera_springboot.dto.auth.LoginDto;
import unicam.progetto_filiera_springboot.dto.auth.RegisterDto;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.UtenteRegistrato;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;
import unicam.progetto_filiera_springboot.domain.model.Utente;              // <-- Entity JPA
import unicam.progetto_filiera_springboot.repository.UtenteRepository;
import unicam.progetto_filiera_springboot.strategy.validation.*;


// Attori dominio (gerarchia non-JPA)
import unicam.progetto_filiera_springboot.domain.actor.UtenteAutenticato;   // base
import unicam.progetto_filiera_springboot.domain.actor.Acquirente;
import unicam.progetto_filiera_springboot.domain.actor.Produttore;
import unicam.progetto_filiera_springboot.domain.actor.Trasformatore;
import unicam.progetto_filiera_springboot.domain.actor.DistributoreTipicita;
import unicam.progetto_filiera_springboot.domain.actor.Curatore;
import unicam.progetto_filiera_springboot.domain.actor.Animatore;
import unicam.progetto_filiera_springboot.domain.actor.GestorePiattaforma;

import unicam.progetto_filiera_springboot.domain.factory.Attore;
import unicam.progetto_filiera_springboot.domain.factory.AttoreFactory;

@Service
public class AuthService {

    private final UtenteRepository repo;
    private final EventPublisher eventPublisher;   // <--- bean Spring

    private final List<ValidationStrategy<RegisterDto>> registerValidators;
    private final ValidationStrategy<LoginDto> loginValidator = new LoginRequiredFieldsValidation();

    public AuthService(UtenteRepository repo, EventPublisher eventPublisher) {
        this.repo = repo;
        this.eventPublisher = eventPublisher;
        this.registerValidators = List.of(
                new RegisterRequiredFieldsValidation(),
                new PasswordStrengthValidation()
        );
    }

    // ---------------- REGISTRAZIONE ----------------
    @Transactional
    public String register(RegisterDto dto) {
        // Strategy: validazione
        registerValidators.forEach(v -> v.validate(dto));

        // Check duplicato
        Ruolo ruolo = dto.getRuolo();
        if (repo.existsByUsernameAndRuolo(dto.getUsername(), ruolo)) {
            throw new ValidationException("Username già presente per il ruolo selezionato");
        }

        // Factory Method
        Attore attore = AttoreFactory.crea(ruolo);

        // Mapping DTO -> Entity
        Utente entity = new Utente(
                dto.getUsername(),
                dto.getPassword(),
                dto.getNome(),
                dto.getCognome(),
                ruolo
        );
        Utente saved = repo.save(entity);

        // Observer: evento dominio post-registrazione
        eventPublisher.publish(new UtenteRegistrato(saved.getUsername()));

        return "Registrazione completata!";
    }

    // ---------------- LOGIN (compatibilità: restituisce Entity JPA) ----------------
    @Transactional(readOnly = true)
    public Utente login(LoginDto dto) {
        // Strategy: validazione campi
        loginValidator.validate(dto);

        // Recupera per username + ruolo
        Optional<Utente> opt = repo.findByUsernameAndRuolo(dto.getUsername(), dto.getRuolo());
        if (opt.isEmpty()) {
            throw new ValidationException("Credenziali errate o ruolo non corrispondente.");
        }

        Utente u = opt.get();

        // Verifica password
        if (!u.getPassword().equals(dto.getPassword())) {
            throw new ValidationException("Credenziali errate o ruolo non corrispondente.");
        }

        return u;
    }

    // ---------------- LOGIN (nuovo): restituisce ATTORI dominio ----------------
    @Transactional(readOnly = true)
    public UtenteAutenticato loginAsActor(LoginDto dto) {
        // Strategy: validazione campi
        loginValidator.validate(dto);

        // Recupera per username + ruolo
        Utente e = repo.findByUsernameAndRuolo(dto.getUsername(), dto.getRuolo())
                .orElseThrow(() -> new ValidationException("Credenziali errate o ruolo non corrispondente."));

        // Verifica password
        if (!e.getPassword().equals(dto.getPassword())) {
            throw new ValidationException("Credenziali errate o ruolo non corrispondente.");
        }

        // Mapping Entity -> Attore dominio
        return toDomainActor(e);
    }

    // ---------------- Helper: mapping Entity -> Dominio ----------------
    private UtenteAutenticato toDomainActor(Utente e) {
        return switch (e.getRuolo()) {
            case PRODUTTORE -> new Produttore(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case TRASFORMATORE -> new Trasformatore(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case DISTRIBUTORE_TIPICITA ->
                    new DistributoreTipicita(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case CURATORE -> new Curatore(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case ANIMATORE -> new Animatore(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case GESTORE_PIATTAFORMA ->
                    new GestorePiattaforma(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome());
            case ACQUIRENTE -> {
                double fondi = 0.0;
                yield new Acquirente(e.getUsername(), e.getPassword(), e.getNome(), e.getCognome(), fondi);
            }
        };
    }
}
