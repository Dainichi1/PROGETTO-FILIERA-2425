package unicam.filiera.factory;

import unicam.filiera.dto.*;
import unicam.filiera.model.*;
import unicam.filiera.model.StatoProdotto;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ItemFactory {

    private ItemFactory() {}

    // Tipizza su BaseItemDto per evitare Object
    public static record Data(BaseItemDto dto, String creatore) {}

    private static final Map<ItemTipo, Function<Data, Item>> registry = new EnumMap<>(ItemTipo.class);

    static {
        // --- PRODOTTO ---
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

        // --- PACCHETTO ---
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
                    .prodottiIds( // <-- usa gli ID Long del DTO
                            dto.getProdottiSelezionati() == null ? java.util.List.of() : dto.getProdottiSelezionati()
                    )
                    .certificati(toOriginalNames(dto.getCertificati()))
                    .foto(toOriginalNames(dto.getFoto()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });

        // --- TRASFORMATO ---
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
                            .collect(Collectors.toList()))
                    .certificati(toOriginalNames(dto.getCertificati()))
                    .foto(toOriginalNames(dto.getFoto()))
                    .creatoDa(data.creatore())
                    .stato(StatoProdotto.IN_ATTESA)
                    .commento(null)
                    .build();
        });
    }

    // Ignora file null, vuoti o con filename blank
    private static List<String> toOriginalNames(List<MultipartFile> files) {
        return (files == null)
                ? List.of()
                : files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(MultipartFile::getOriginalFilename)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.toList());
    }

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

    // Overload opzionale
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
}
