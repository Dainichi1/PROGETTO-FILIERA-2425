package unicam.filiera.factory;

import unicam.filiera.dto.BaseEventoDto;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.dto.EventoTipo;
import unicam.filiera.entity.FieraEntity;
import unicam.filiera.entity.VisitaInvitoEntity;
import unicam.filiera.model.Evento;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.Fiera;
import unicam.filiera.repository.FieraRepository;
import unicam.filiera.repository.VisitaInvitoRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Factory centrale per la creazione e ricostruzione di eventi
 * (VisitaInvito, Fiera, …).
 */
public final class EventoFactory {

    private EventoFactory() {}

    public static record Data(BaseEventoDto dto, String creatore) {}

    private static final Map<EventoTipo, Function<Data, Evento>> registry = new EnumMap<>(EventoTipo.class);

    static {
        // === VISITA INVITO ===
        registry.put(EventoTipo.VISITA, data -> {
            if (!(data.dto() instanceof VisitaInvitoDto dto)) {
                throw new IllegalArgumentException("DTO non valido per VISITA");
            }
            if (dto.getDestinatari() == null || dto.getDestinatari().isEmpty()) {
                throw new IllegalArgumentException("⚠ Devi selezionare almeno un destinatario");
            }
            return new VisitaInvito.Builder()
                    .id(dto.getId())
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .indirizzo(dto.getIndirizzo())
                    .dataInizio(dto.getDataInizio())
                    .dataFine(dto.getDataFine())
                    .creatoDa(data.creatore())
                    .stato(StatoEvento.PUBBLICATA)
                    .destinatari(List.copyOf(dto.getDestinatari()))
                    .build();
        });

        // === FIERA ===
        registry.put(EventoTipo.FIERA, data -> {
            if (!(data.dto() instanceof FieraDto dto)) {
                throw new IllegalArgumentException("DTO non valido per FIERA");
            }
            if (dto.getPrezzo() < 0) {
                throw new IllegalArgumentException("⚠ Il prezzo non può essere negativo");
            }
            return new Fiera.Builder()
                    .id(dto.getId())
                    .nome(dto.getNome())
                    .descrizione(dto.getDescrizione())
                    .indirizzo(dto.getIndirizzo())
                    .dataInizio(dto.getDataInizio())
                    .dataFine(dto.getDataFine())
                    .creatoDa(data.creatore())
                    .stato(StatoEvento.PUBBLICATA)
                    .prezzo(dto.getPrezzo())
                    .build();
        });
    }

    /* =========================
       Creazione da DTO
    ========================= */
    public static Evento creaEvento(BaseEventoDto dto, String creatore) {
        if (dto == null || dto.getTipo() == null) {
            throw new IllegalArgumentException("DTO o tipo evento null");
        }
        Function<Data, Evento> creator = registry.get(dto.getTipo());
        if (creator == null) {
            throw new UnsupportedOperationException("Tipo evento non supportato: " + dto.getTipo());
        }
        return creator.apply(new Data(dto, creatore));
    }

    public static Evento creaEvento(EventoTipo tipo, BaseEventoDto dto, String creatore) {
        Function<Data, Evento> creator = registry.get(tipo);
        if (creator == null) {
            throw new UnsupportedOperationException("Tipo evento non supportato: " + tipo);
        }
        return creator.apply(new Data(dto, creatore));
    }

    public static VisitaInvito creaVisitaInvito(VisitaInvitoDto dto, String creatore) {
        return (VisitaInvito) creaEvento(dto, creatore);
    }

    public static Fiera creaFiera(FieraDto dto, String creatore) {
        return (Fiera) creaEvento(dto, creatore);
    }

    /* =========================
       Ricostruzione da Entity con tipo esplicito
    ========================= */
    public static Evento fromId(Long id,
                                EventoTipo tipo,
                                FieraRepository fieraRepo,
                                VisitaInvitoRepository visitaRepo) {
        if (id == null || tipo == null) {
            throw new IllegalArgumentException("Id e tipo non possono essere null");
        }

        return switch (tipo) {
            case FIERA -> fieraRepo.findById(id)
                    .map(e -> new Fiera.Builder()
                            .id(e.getId())
                            .nome(e.getNome())
                            .descrizione(e.getDescrizione())
                            .indirizzo(e.getIndirizzo())
                            .dataInizio(e.getDataInizio())
                            .dataFine(e.getDataFine())
                            .creatoDa(e.getCreatoDa())
                            .stato(e.getStato())
                            .prezzo(e.getPrezzo())
                            .build())
                    .orElseThrow(() -> new IllegalArgumentException("Fiera con id=" + id + " non trovata"));

            case VISITA -> visitaRepo.findById(id)
                    .map(e -> {
                        List<String> destinatari = (e.getDestinatari() != null && !e.getDestinatari().isBlank())
                                ? List.of(e.getDestinatari().split(","))
                                : List.of();

                        return new VisitaInvito.Builder()
                                .id(e.getId())
                                .nome(e.getNome())
                                .descrizione(e.getDescrizione())
                                .indirizzo(e.getIndirizzo())
                                .dataInizio(e.getDataInizio())
                                .dataFine(e.getDataFine())
                                .creatoDa(e.getCreatoDa())
                                .stato(e.getStato())
                                .destinatari(destinatari)
                                .build();
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Visita con id=" + id + " non trovata"));
        };
    }
}
