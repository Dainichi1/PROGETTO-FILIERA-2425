package unicam.filiera.service;

import org.springframework.stereotype.Service;
import unicam.filiera.model.CategoriaContenuto;
import unicam.filiera.model.CriteriRicerca;
import unicam.filiera.dto.ElementoPiattaformaDto;
import unicam.filiera.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GestoreContenutiServiceImpl implements GestoreContenutiService {

    private final UtenteRepository utenteRepo;
    private final ProdottoRepository prodottoRepo;
    private final PacchettoRepository pacchettoRepo;
    private final ProdottoTrasformatoRepository trasformatoRepo;
    private final FieraRepository fieraRepo;
    private final VisitaInvitoRepository visitaRepo;
    private final AcquistoRepository acquistoRepo;
    private final PrenotazioneFieraRepository prenotazioneFieraRepo;
    private final PrenotazioneVisitaRepository prenotazioneVisitaRepo;
    private final PostSocialRepository postRepo;

    public GestoreContenutiServiceImpl(
            UtenteRepository utenteRepo,
            ProdottoRepository prodottoRepo,
            PacchettoRepository pacchettoRepo,
            ProdottoTrasformatoRepository trasformatoRepo,
            FieraRepository fieraRepo,
            VisitaInvitoRepository visitaRepo,
            AcquistoRepository acquistoRepo,
            PrenotazioneFieraRepository prenotazioneFieraRepo,
            PrenotazioneVisitaRepository prenotazioneVisitaRepo,
            PostSocialRepository postRepo
    ) {
        this.utenteRepo = utenteRepo;
        this.prodottoRepo = prodottoRepo;
        this.pacchettoRepo = pacchettoRepo;
        this.trasformatoRepo = trasformatoRepo;
        this.fieraRepo = fieraRepo;
        this.visitaRepo = visitaRepo;
        this.acquistoRepo = acquistoRepo;
        this.prenotazioneFieraRepo = prenotazioneFieraRepo;
        this.prenotazioneVisitaRepo = prenotazioneVisitaRepo;
        this.postRepo = postRepo;
    }

    @Override
    public List<CategoriaContenuto> getCategorieContenuti() {
        return Arrays.asList(CategoriaContenuto.values());
    }

    @Override
    public List<ElementoPiattaformaDto> getContenutiCategoria(CategoriaContenuto cat) {
        return switch (cat) {
            case UTENTI -> utenteRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(e.getUsername())
                            .nome(e.getNome() + " " + e.getCognome())
                            .tipo("Utente")
                            .stato(e.getRuolo().name())
                            .extra("Fondi: " + (e.getFondi() != null ? e.getFondi() : 0.0))
                            .build())
                    .toList();

            case PRODOTTI -> prodottoRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getNome())
                            .tipo("Prodotto")
                            .stato(e.getStato().name())
                            .extra("Prezzo: " + e.getPrezzo() + " | " + e.getIndirizzo())
                            .build())
                    .toList();

            case PACCHETTI -> pacchettoRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getNome())
                            .tipo("Pacchetto")
                            .stato(e.getStato().name())
                            .extra("Prezzo: " + e.getPrezzo() + " | " + e.getIndirizzo())
                            .build())
                    .toList();

            case PRODOTTI_TRASFORMATI -> trasformatoRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getNome())
                            .tipo("Prodotto trasformato")
                            .stato(e.getStato().name())
                            .extra("Prezzo: " + e.getPrezzo() + " | " + e.getIndirizzo())
                            .build())
                    .toList();

            case FIERE -> fieraRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getDescrizione())
                            .tipo("Fiera")
                            .stato(e.getStato().name())
                            .data(e.getDataInizio() != null ? e.getDataInizio().atStartOfDay() : null)
                            .extra("Org: " + e.getCreatoDa())
                            .build())
                    .toList();

            case VISITE_INVITO -> visitaRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getDescrizione())
                            .tipo("Visita su invito")
                            .stato(e.getStato().name())
                            .data(e.getDataInizio() != null ? e.getDataInizio().atStartOfDay() : null)
                            .extra("Org: " + e.getCreatoDa())
                            .build())
                    .toList();

            case ACQUISTI -> acquistoRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome("Acquirente: " + e.getUsernameAcquirente())
                            .tipo("Acquisto")
                            .stato(e.getStatoPagamento().name())
                            .data(e.getDataOra())
                            .extra("Totale: " + e.getTotale())
                            .build())
                    .toList();

            case PRENOTAZIONI_FIERE -> prenotazioneFieraRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome("Acquirente: " + e.getUsernameAcquirente())
                            .tipo("Prenotazione fiera")
                            .data(e.getDataPrenotazione())
                            .extra("Persone: " + e.getNumeroPersone())
                            .build())
                    .toList();

            case PRENOTAZIONI_VISITE -> prenotazioneVisitaRepo.findAll().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome("Venditore: " + e.getUsernameVenditore())
                            .tipo("Prenotazione visita")
                            .data(e.getDataPrenotazione())
                            .extra("Persone: " + e.getNumeroPersone())
                            .build())
                    .toList();

            case SOCIAL_POSTS -> postRepo.findAllByOrderByCreatedAtDesc().stream()
                    .map(e -> ElementoPiattaformaDto.builder()
                            .id(String.valueOf(e.getId()))
                            .nome(e.getTitolo() != null ? e.getTitolo() : e.getTesto())
                            .tipo("Post")
                            .stato("Pubblicato")
                            .data(e.getCreatedAt())
                            .extra("Autore: " + e.getAutoreUsername())
                            .build())
                    .toList();
        };
    }

    @Override
    public List<ElementoPiattaformaDto> filtraOrdinaLista(List<ElementoPiattaformaDto> src, CriteriRicerca c) {
        if (src == null) return List.of();
        var stream = src.stream();

        if (c != null) {
            if (c.getTesto() != null && !c.getTesto().isBlank()) {
                final String t = c.getTesto().toLowerCase();
                stream = stream.filter(e ->
                        (e.getNome() != null && e.getNome().toLowerCase().contains(t)) ||
                                (e.getExtra() != null && e.getExtra().toLowerCase().contains(t))
                );
            }
            if (c.getStato() != null && !c.getStato().isBlank() && !"Tutti".equalsIgnoreCase(c.getStato())) {
                final String s = c.getStato().toLowerCase();
                stream = stream.filter(e -> e.getStato() != null && e.getStato().toLowerCase().contains(s));
            }
        }

        Comparator<ElementoPiattaformaDto> cmp = Comparator.comparing(ElementoPiattaformaDto::getId);

        if (c != null && c.getOrdinamento() != null) {
            switch (c.getOrdinamento().toUpperCase()) {
                case "DATA" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getData,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                case "NOME" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getNome,
                        Comparator.nullsLast(String::compareToIgnoreCase));
                case "STATO" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getStato,
                        Comparator.nullsLast(String::compareToIgnoreCase));
                case "TIPO" -> cmp = Comparator.comparing(ElementoPiattaformaDto::getTipo,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            }
        }

        return (c == null || c.isCrescente())
                ? stream.sorted(cmp).collect(Collectors.toList())
                : stream.sorted(cmp.reversed()).collect(Collectors.toList());
    }

    @Override
    public String[] getPossibiliStati(CategoriaContenuto cat) {
        return switch (cat) {
            case PRODOTTI, PACCHETTI, PRODOTTI_TRASFORMATI ->
                    new String[]{"Tutti", "IN_ATTESA", "APPROVATO", "RIFIUTATO"};
            case FIERE, VISITE_INVITO ->
                    new String[]{"Tutti", "IN_PREPARAZIONE", "PUBBLICATA", "CANCELLATA"};
            case ACQUISTI ->
                    new String[]{"Tutti", "IN_ATTESA", "APPROVATO", "RIFIUTATO"};
            default -> new String[]{"Tutti"};
        };
    }
}
