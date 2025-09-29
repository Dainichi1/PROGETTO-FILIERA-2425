package unicam.filiera.factory;

import unicam.filiera.dto.*;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.model.*;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.ProdottoTrasformatoRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory centrale per la creazione e ricostruzione degli Item
 * (Prodotto, Pacchetto, ProdottoTrasformato).
 */
public final class ItemFactory {

    private ItemFactory() {}

    /** Wrapper dati di input */
    public static record Data(BaseItemDto dto, String creatore) {}

    private static final Map<ItemTipo, Function<Data, Item>> registry = new EnumMap<>(ItemTipo.class);

    static {
        // === PRODOTTO ===
        registry.put(ItemTipo.PRODOTTO, data -> {
            if (!(data.dto() instanceof ProdottoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per PRODOTTO");
            }
            return new Prodotto.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .certificati(toOriginalNames(dto.getCertificati()))
                    .foto(toOriginalNames(dto.getFoto()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });

        // === PACCHETTO ===
        registry.put(ItemTipo.PACCHETTO, data -> {
            if (!(data.dto() instanceof PacchettoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per PACCHETTO");
            }
            return new Pacchetto.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .prodottiIds(dto.getProdottiSelezionati() == null ? List.of() : dto.getProdottiSelezionati())
                    .certificati(toOriginalNames(dto.getCertificati()))
                    .foto(toOriginalNames(dto.getFoto()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });

        // === TRASFORMATO ===
        registry.put(ItemTipo.TRASFORMATO, data -> {
            if (!(data.dto() instanceof ProdottoTrasformatoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per TRASFORMATO");
            }
            return new ProdottoTrasformato.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .fasiProduzione(dto.getFasiProduzione() == null
                            ? List.of()
                            : dto.getFasiProduzione().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(FaseProduzione::fromDto)
                            .filter(f -> f != null
                                    && f.getDescrizioneFase() != null && !f.getDescrizioneFase().isBlank()
                                    && f.getProduttoreUsername() != null && !f.getProduttoreUsername().isBlank()
                                    && f.getProdottoOrigineId() != null)
                            .toList())
                    .certificati(toOriginalNames(dto.getCertificati()))
                    .foto(toOriginalNames(dto.getFoto()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });
    }

    /** Utility: estrae i nomi file validi da MultipartFile */
    private static List<String> toOriginalNames(List<MultipartFile> files) {
        return (files == null)
                ? List.of()
                : files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(MultipartFile::getOriginalFilename)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.toList());
    }

    /* =========================
       Creazione da DTO
    ========================= */
    public static Item creaItem(BaseItemDto dto, String creatore) {
        if (dto == null || dto.getTipo() == null) {
            throw new IllegalArgumentException("DTO o tipo null");
        }
        Function<Data, Item> creator = registry.get(dto.getTipo());
        if (creator == null) {
            throw new UnsupportedOperationException("Tipo Item non supportato: " + dto.getTipo());
        }
        return creator.apply(new Data(dto, creatore));
    }

    public static Item creaItem(ItemTipo tipo, BaseItemDto dto, String creatore) {
        Function<Data, Item> creator = registry.get(tipo);
        if (creator == null) {
            throw new UnsupportedOperationException("Tipo Item non supportato: " + tipo);
        }
        return creator.apply(new Data(dto, creatore));
    }

    public static Prodotto creaProdotto(ProdottoDto dto, String creatore) {
        return (Prodotto) creaItem(dto, creatore);
    }

    public static Pacchetto creaPacchetto(PacchettoDto dto, String creatore) {
        return (Pacchetto) creaItem(dto, creatore);
    }

    public static ProdottoTrasformato creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
        return (ProdottoTrasformato) creaItem(dto, creatore);
    }

    /* =========================
       Ricostruzione da Entity con tipo esplicito
    ========================= */
    public static Item fromId(Long id,
                              ItemTipo tipo,
                              ProdottoRepository prodottoRepo,
                              PacchettoRepository pacchettoRepo,
                              ProdottoTrasformatoRepository trasformatoRepo) {
        if (id == null || tipo == null) {
            throw new IllegalArgumentException("Id e tipo non possono essere null");
        }

        return switch (tipo) {
            case PRODOTTO -> prodottoRepo.findById(id)
                    .map(e -> new Prodotto.Builder()
                            .id(e.getId())
                            .nome(e.getNome())
                            .descrizione(e.getDescrizione())
                            .indirizzo(e.getIndirizzo())
                            .quantita(e.getQuantita())
                            .prezzo(e.getPrezzo())
                            .creatoDa(e.getCreatoDa())
                            .stato(e.getStato())
                            .commento(e.getCommento())
                            .build())
                    .orElseThrow(() -> new IllegalArgumentException("Prodotto con id=" + id + " non trovato"));

            case PACCHETTO -> pacchettoRepo.findById(id)
                    .map(e -> new Pacchetto.Builder()
                            .id(e.getId())
                            .nome(e.getNome())
                            .descrizione(e.getDescrizione())
                            .indirizzo(e.getIndirizzo())
                            .quantita(e.getQuantita())
                            .prezzo(e.getPrezzo())
                            .creatoDa(e.getCreatoDa())
                            .stato(e.getStato())
                            .commento(e.getCommento())
                            .prodottiIds(
                                    e.getProdotti() != null
                                            ? e.getProdotti().stream().map(ProdottoEntity::getId).toList()
                                            : List.of()
                            )
                            .build())
                    .orElseThrow(() -> new IllegalArgumentException("Pacchetto con id=" + id + " non trovato"));

            case TRASFORMATO -> trasformatoRepo.findById(id)
                    .map(e -> new ProdottoTrasformato.Builder()
                            .id(e.getId())
                            .nome(e.getNome())
                            .descrizione(e.getDescrizione())
                            .indirizzo(e.getIndirizzo())
                            .quantita(e.getQuantita())
                            .prezzo(e.getPrezzo())
                            .creatoDa(e.getCreatoDa())
                            .stato(e.getStato())
                            .commento(e.getCommento())
                            .fasiProduzione(
                                    e.getFasiProduzione() != null
                                            ? e.getFasiProduzione().stream()
                                            .map(f -> new FaseProduzione(
                                                    f.getDescrizioneFase(),
                                                    f.getProduttoreUsername(),
                                                    f.getProdottoOrigineId()
                                            ))
                                            .toList()
                                            : List.of()
                            )
                            .build())
                    .orElseThrow(() -> new IllegalArgumentException("Trasformato con id=" + id + " non trovato"));
        };
    }

    /* =========================
       Variante sicura con Optional
    ========================= */
    public static Optional<Item> tryFromId(Long id,
                                           ItemTipo tipo,
                                           ProdottoRepository prodottoRepo,
                                           PacchettoRepository pacchettoRepo,
                                           ProdottoTrasformatoRepository trasformatoRepo) {
        try {
            return Optional.of(fromId(id, tipo, prodottoRepo, pacchettoRepo, trasformatoRepo));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
