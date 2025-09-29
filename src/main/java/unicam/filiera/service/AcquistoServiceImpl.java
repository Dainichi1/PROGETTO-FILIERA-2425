package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.AcquistoItemDto;
import unicam.filiera.dto.AcquistoListaDto;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.entity.AcquistoEntity;
import unicam.filiera.entity.AcquistoItemEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcquistoServiceImpl implements AcquistoService {

    private static final Logger log = LoggerFactory.getLogger(AcquistoServiceImpl.class);

    private final AcquistoRepository acquistoRepo;
    private final AcquistoItemRepository itemRepo;
    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;
    private final ProdottoTrasformatoRepository prodottoTrasformatoRepository;
    private final UtenteRepository utenteRepository;

    public AcquistoServiceImpl(AcquistoRepository acquistoRepo,
                               AcquistoItemRepository itemRepo,
                               ProdottoRepository prodottoRepository,
                               PacchettoRepository pacchettoRepository,
                               ProdottoTrasformatoRepository prodottoTrasformatoRepository,
                               UtenteRepository utenteRepository) {
        this.acquistoRepo = acquistoRepo;
        this.itemRepo = itemRepo;
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoTrasformatoRepository = prodottoTrasformatoRepository;
        this.utenteRepository = utenteRepository;
    }

    @Override
    public void salvaAcquisto(DatiAcquistoDto dto) {
        log.info(">>> Salvataggio nuovo acquisto per utente [{}]", dto.getUsernameAcquirente());

        // Ricalcolo totale lato backend
        double totaleCalcolato = dto.getItems().stream()
                .mapToDouble(i -> i.getPrezzoUnitario() * i.getQuantita())
                .sum();
        dto.setTotaleAcquisto(totaleCalcolato);

        // Recupero utente
        UtenteEntity utente = utenteRepository.findByUsername(dto.getUsernameAcquirente())
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        double fondiPre = utente.getFondi();
        double fondiPost = fondiPre - totaleCalcolato;

        if (fondiPost < 0) {
            throw new IllegalArgumentException("Fondi insufficienti");
        }

        utente.setFondi(fondiPost);
        utenteRepository.save(utente);

        dto.setFondiPreAcquisto(fondiPre);
        dto.setFondiPostAcquisto(fondiPost);

        // Salvo Acquisto
        AcquistoEntity acquisto = new AcquistoEntity();
        acquisto.setUsernameAcquirente(dto.getUsernameAcquirente());
        acquisto.setTotale(dto.getTotaleAcquisto());
        acquisto.setStatoPagamento(dto.getStatoPagamento());
        acquisto.setTipoMetodoPagamento(dto.getTipoMetodoPagamento());
        acquisto.setDataOra(dto.getTimestamp());
        acquisto.setFondiPreAcquisto(fondiPre);
        acquisto.setFondiPostAcquisto(fondiPost);

        // Costruisco elencoItem
        String elencoItem = dto.getItems().stream()
                .map(i -> i.getNome() + " x" + i.getQuantita())
                .collect(Collectors.joining(", "));
        acquisto.setElencoItem(elencoItem);

        AcquistoEntity saved = acquistoRepo.save(acquisto);

        // Salvo items e aggiorno disponibilità
        for (CartItemDto i : dto.getItems()) {
            AcquistoItemEntity e = new AcquistoItemEntity();
            e.setAcquisto(saved);
            e.setNomeItem(i.getNome());
            e.setTipoItem(i.getTipo().name());
            e.setQuantita(i.getQuantita());
            e.setPrezzoUnitario(i.getPrezzoUnitario());
            e.setTotale(i.getPrezzoUnitario() * i.getQuantita());
            itemRepo.save(e);

            switch (i.getTipo()) {
                case PRODOTTO -> aggiornaDisponibilitaProdotto(i.getId(), i.getQuantita(), i.getNome());
                case PACCHETTO -> aggiornaDisponibilitaPacchetto(i.getId(), i.getQuantita(), i.getNome());
                case TRASFORMATO -> aggiornaDisponibilitaTrasformato(i.getId(), i.getQuantita(), i.getNome());
            }
        }

        log.info("✅ Acquisto completato: ID={} - Totale={}", saved.getId(), totaleCalcolato);
    }

    // ========== Helper privati per gestire disponibilità ==========
    private void aggiornaDisponibilitaProdotto(Long id, int quantita, String nome) {
        int disponibile = prodottoRepository.findById(id)
                .map(p -> p.getQuantita())
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        if (disponibile < quantita) {
            throw new IllegalArgumentException("Disponibilità insufficiente per prodotto '" + nome + "'");
        }

        prodottoRepository.decrementaQuantita(id, quantita);
        log.info("📉 Prodotto [{}] aggiornato: nuova quantità = {}", nome, disponibile - quantita);
    }

    private void aggiornaDisponibilitaPacchetto(Long id, int quantita, String nome) {
        int disponibile = pacchettoRepository.findById(id)
                .map(p -> p.getQuantita())
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        if (disponibile < quantita) {
            throw new IllegalArgumentException("Disponibilità insufficiente per pacchetto '" + nome + "'");
        }

        pacchettoRepository.decrementaQuantita(id, quantita);
        log.info("📉 Pacchetto [{}] aggiornato: nuova quantità = {}", nome, disponibile - quantita);
    }

    private void aggiornaDisponibilitaTrasformato(Long id, int quantita, String nome) {
        int disponibile = prodottoTrasformatoRepository.findById(id)
                .map(p -> p.getQuantita())
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));

        if (disponibile < quantita) {
            throw new IllegalArgumentException("Disponibilità insufficiente per trasformato '" + nome + "'");
        }

        prodottoTrasformatoRepository.decrementaQuantita(id, quantita);
        log.info("📉 Trasformato [{}] aggiornato: nuova quantità = {}", nome, disponibile - quantita);
    }

    @Override
    public List<AcquistoListaDto> getAcquistiByUsername(String username) {
        log.debug("Recupero acquisti per utente [{}]", username);
        return acquistoRepo.findByUsernameAcquirenteOrderByDataOraDesc(username)
                .stream()
                .map(e -> new AcquistoListaDto(
                        e.getId(),
                        e.getUsernameAcquirente(),
                        e.getTotale(),
                        e.getStatoPagamento().name(),
                        e.getTipoMetodoPagamento().name(),
                        e.getDataOra(),
                        e.getElencoItem()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<AcquistoItemDto> getItemsByAcquisto(Long id) {
        log.debug("Recupero items per acquisto id={}", id);
        return itemRepo.findByAcquistoId(id)
                .stream()
                .map(e -> new AcquistoItemDto(
                        e.getNomeItem(),
                        e.getTipoItem(),
                        e.getQuantita(),
                        e.getPrezzoUnitario(),
                        e.getTotale()
                ))
                .collect(Collectors.toList());
    }
}
