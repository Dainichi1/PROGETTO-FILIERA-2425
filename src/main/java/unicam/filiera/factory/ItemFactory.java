package unicam.filiera.factory;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.*;
import unicam.filiera.model.StatoProdotto;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory centralizzata per la creazione di {@link Item} (Prodotto, Pacchetto, ProdottoTrasformato)
 * a partire dai rispettivi DTO.
 *
 * Usa un registry di strategie (Strategy + Factory Method).
 */
public final class ItemFactory {

    private ItemFactory() {}

    public enum TipoItem {
        PRODOTTO,
        PACCHETTO,
        PRODOTTO_TRASFORMATO
    }

    public static record Data(Object dto, String creatore) {}

    private static final Map<TipoItem, Function<Data, Item>> registry =
            new EnumMap<>(TipoItem.class);

    static {
        // --- Strategia: PRODOTTO ---
        registry.put(TipoItem.PRODOTTO, data -> {
            if (!(data.dto() instanceof ProdottoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per PRODOTTO");
            }

            return new Prodotto.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .certificati(dto.getCertificati() == null ?
                            java.util.List.of() : dto.getCertificati().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .foto(dto.getFoto() == null ?
                            java.util.List.of() : dto.getFoto().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });

        // --- Strategia: PACCHETTO ---
        registry.put(TipoItem.PACCHETTO, data -> {
            if (!(data.dto() instanceof PacchettoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per PACCHETTO");
            }

            return new Pacchetto.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .prodotti(dto.getProdottiSelezionati() == null
                            ? java.util.List.of()
                            : dto.getProdottiSelezionati().stream()
                            .map(Object::toString)
                            .collect(Collectors.toList()))
                    .certificati(dto.getCertificati() == null ?
                            java.util.List.of() : dto.getCertificati().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .foto(dto.getFoto() == null ?
                            java.util.List.of() : dto.getFoto().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });

        // --- Strategia: PRODOTTO_TRASFORMATO ---
        registry.put(TipoItem.PRODOTTO_TRASFORMATO, data -> {
            if (!(data.dto() instanceof ProdottoTrasformatoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per PRODOTTO_TRASFORMATO");
            }

            return new ProdottoTrasformato.Builder()
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .quantita(dto.getQuantita())
                    .prezzo(dto.getPrezzo())
                    .indirizzo(dto.getIndirizzo())
                    .fasiProduzione(dto.getFasiProduzione() == null
                            ? java.util.List.of()
                            : dto.getFasiProduzione().stream()
                            .map(FaseProduzione::fromDto)   // usa il convertitore corretto
                            .collect(Collectors.toList()))
                    .certificati(dto.getCertificati() == null ?
                            java.util.List.of() : dto.getCertificati().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .foto(dto.getFoto() == null ?
                            java.util.List.of() : dto.getFoto().stream()
                            .map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });
    }

    public static void register(TipoItem tipo, Function<Data, Item> creator) {
        registry.put(tipo, creator);
    }

    public static Item creaItem(TipoItem tipo, Object dto, String creatore) {
        Data d = new Data(dto, creatore);
        Function<Data, Item> creator = registry.get(tipo);
        if (creator == null) {
            throw new UnsupportedOperationException("Tipo Item non supportato: " + tipo);
        }
        return creator.apply(d);
    }

    public static Prodotto creaProdotto(ProdottoDto dto, String creatore) {
        return (Prodotto) creaItem(TipoItem.PRODOTTO, dto, creatore);
    }

    public static Pacchetto creaPacchetto(PacchettoDto dto, String creatore) {
        return (Pacchetto) creaItem(TipoItem.PACCHETTO, dto, creatore);
    }

    public static ProdottoTrasformato creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
        return (ProdottoTrasformato) creaItem(TipoItem.PRODOTTO_TRASFORMATO, dto, creatore);
    }
}
