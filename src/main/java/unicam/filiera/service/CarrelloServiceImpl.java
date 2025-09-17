package unicam.filiera.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.model.*;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.ProdottoTrasformatoRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarrelloServiceImpl implements CarrelloService {

    private static final String SESSION_KEY = "CART_ITEMS";

    private final ProdottoRepository prodottoRepository;
    private final ProdottoTrasformatoRepository trasformatoRepository;
    private final PacchettoRepository pacchettoRepository;

    @Autowired
    public CarrelloServiceImpl(ProdottoRepository prodottoRepository,
                               ProdottoTrasformatoRepository trasformatoRepository,
                               PacchettoRepository pacchettoRepository) {
        this.prodottoRepository = prodottoRepository;
        this.trasformatoRepository = trasformatoRepository;
        this.pacchettoRepository = pacchettoRepository;
    }

    @Override
    public void aggiungiItem(ItemTipo tipo, Long id, int quantita, HttpSession session) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo item mancante");
        }

        List<CartItemDto> items = getOrInitCart(session);

        Item item = caricaItem(tipo, id);
        int disponibilitaMagazzino = item.getQuantita();

        if (quantita > disponibilitaMagazzino) {
            throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità (" + disponibilitaMagazzino + ")");
        }

        CartItemDto existing = items.stream()
                .filter(i -> i.getId().equals(item.getId()) && i.getTipo() == tipo)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int nuovaQuantita = existing.getQuantita() + quantita;
            if (nuovaQuantita > disponibilitaMagazzino) {
                throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità (" + disponibilitaMagazzino + ")");
            }

            existing.setDisponibilita(disponibilitaMagazzino);
            existing.setQuantita(nuovaQuantita);
            existing.setDisponibilita(Math.max(0, disponibilitaMagazzino - nuovaQuantita));
            existing.recalculateTotale();

        } else {
            CartItemDto dto = CartItemDto.builder()
                    .tipo(tipo)
                    .id(item.getId())
                    .nome(item.getNome())
                    .prezzoUnitario(item.getPrezzo())
                    .build();

            dto.setDisponibilita(disponibilitaMagazzino);
            dto.setQuantita(quantita);
            dto.setDisponibilita(Math.max(0, disponibilitaMagazzino - quantita));
            dto.recalculateTotale();

            items.add(dto);
        }

        session.setAttribute(SESSION_KEY, items);
    }

    @Override
    public void aggiornaQuantitaItem(String nomeItem, int nuovaQuantita, HttpSession session) {
        List<CartItemDto> items = getOrInitCart(session);

        CartItemDto itemCarrello = items.stream()
                .filter(i -> i.getNome().equals(nomeItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item non trovato"));

        Item itemDb = caricaItem(itemCarrello.getTipo(), itemCarrello.getId());
        int disponibilitaMagazzino = itemDb.getQuantita();

        if (nuovaQuantita > disponibilitaMagazzino) {
            throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità (" + disponibilitaMagazzino + ")");
        }

        itemCarrello.setQuantita(nuovaQuantita);
        itemCarrello.setDisponibilita(Math.max(0, disponibilitaMagazzino - nuovaQuantita));

        session.setAttribute(SESSION_KEY, items);
    }

    @Override
    public void rimuoviItem(String nomeItem, HttpSession session) {
        List<CartItemDto> items = getOrInitCart(session);
        items.removeIf(i -> i.getNome().equals(nomeItem));
        session.setAttribute(SESSION_KEY, items);
    }

    @Override
    public void svuota(HttpSession session) {
        session.setAttribute(SESSION_KEY, new ArrayList<CartItemDto>());
    }

    @Override
    public List<CartItemDto> getItems(HttpSession session) {
        return List.copyOf(getOrInitCart(session));
    }

    @Override
    public CartTotalsDto calcolaTotali(HttpSession session) {
        List<CartItemDto> items = getOrInitCart(session);
        int totQta = items.stream().mapToInt(CartItemDto::getQuantita).sum();
        double totCost = items.stream().mapToDouble(CartItemDto::getTotale).sum();
        return new CartTotalsDto(totQta, totCost);
    }

    // ======================
    // Helpers
    // ======================
    @SuppressWarnings("unchecked")
    private List<CartItemDto> getOrInitCart(HttpSession session) {
        List<CartItemDto> items = (List<CartItemDto>) session.getAttribute(SESSION_KEY);
        if (items == null) {
            items = new ArrayList<>();
            session.setAttribute(SESSION_KEY, items);
        }
        return items;
    }

    private Item caricaItem(ItemTipo tipo, Long id) {
        return switch (tipo) {
            case PRODOTTO -> prodottoRepository.findById(id)
                    .map(this::mapToDomainProdotto)
                    .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));
            case TRASFORMATO -> trasformatoRepository.findById(id)
                    .map(this::mapToDomainTrasformato)
                    .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));
            case PACCHETTO -> pacchettoRepository.findById(id)
                    .map(this::mapToDomainPacchetto)
                    .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));
        };
    }

    private Prodotto mapToDomainProdotto(ProdottoEntity e) {
        return new Prodotto.Builder()
                .id(e.getId())
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .quantita(e.getQuantita())
                .prezzo(e.getPrezzo())
                .creatoDa(e.getCreatoDa())
                .stato(e.getStato())
                .commento(e.getCommento())
                .build();
    }

    private ProdottoTrasformato mapToDomainTrasformato(ProdottoTrasformatoEntity e) {
        return new ProdottoTrasformato.Builder()
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
                        e.getFasiProduzione().stream()
                                .map(f -> new FaseProduzione(
                                        f.getDescrizioneFase(),
                                        f.getProduttoreUsername(),
                                        f.getProdottoOrigineId()
                                ))
                                .toList()
                )
                .build();
    }

    private Pacchetto mapToDomainPacchetto(PacchettoEntity e) {
        // ProdottiIds dal Set<ProdottoEntity>
        List<Long> prodottiIds = e.getProdotti() != null
                ? e.getProdotti().stream()
                .map(ProdottoEntity::getId)
                .filter(Objects::nonNull)
                .toList()
                : List.of();

        // Converte CSV certificati -> List<String>
        List<String> certificatiList = (e.getCertificati() != null && !e.getCertificati().isBlank())
                ? Arrays.stream(e.getCertificati().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : List.of();

        // Converte CSV foto -> List<String>
        List<String> fotoList = (e.getFoto() != null && !e.getFoto().isBlank())
                ? Arrays.stream(e.getFoto().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : List.of();

        return new Pacchetto.Builder()
                .id(e.getId())
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .quantita(e.getQuantita())
                .prezzo(e.getPrezzo())
                .creatoDa(e.getCreatoDa())
                .stato(e.getStato())
                .commento(e.getCommento())
                .prodottiIds(prodottiIds)
                .certificati(certificatiList)
                .foto(fotoList)
                .build();
    }
}
