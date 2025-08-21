package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;

    @GetMapping
    public String index(@RequestParam(name = "id", required = false) Long id,
                        @RequestParam(name = "t", required = false) String tipoParam,
                        Model model) {

        // 1) carica prodotti approvati
        List<ItemVM> prodotti = prodottoService.listApprovati().stream()
                .map(this::toItemVM)
                .toList();

        // 2) carica pacchetti approvati
        List<ItemVM> pacchetti = pacchettoService.listApprovati().stream()
                .map(this::toItemVM)
                .toList();

        // 3) unisci e ordina (per nome)
        List<ItemVM> items = Stream.concat(prodotti.stream(), pacchetti.stream())
                .sorted(Comparator.comparing(ItemVM::nome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        model.addAttribute("items", items);

        // 4) selezione dettaglio
        ItemType tipoReq = parseTipo(tipoParam);
        ItemVM selected = null;
        if (id != null && tipoReq != null) {
            selected = items.stream()
                    .filter(i -> i.id().equals(id) && i.tipo() == tipoReq)
                    .findFirst()
                    .orElse(null);
        }
        model.addAttribute("selected", selected);

        return "marketplace/index";
    }

    // ---------- mapping ----------
    private ItemVM toItemVM(ProdottoResponse p) {
        return new ItemVM(
                ItemType.PRODOTTO,
                "prodotti",
                p.getId(),
                p.getNome(),
                p.getDescrizione(),
                safePrezzo(p.getPrezzo()),
                p.getIndirizzo(),
                p.getQuantita(),
                p.getCertificati(),
                p.getFoto()
        );
    }

    private ItemVM toItemVM(PacchettoResponse p) {
        // NB: PacchettoResponse espone "prezzoTotale"
        return new ItemVM(
                ItemType.PACCHETTO,
                "pacchetti",
                p.getId(),
                p.getNome(),
                p.getDescrizione(),
                safePrezzo(p.getPrezzoTotale()),
                p.getIndirizzo(),
                p.getQuantita(),
                p.getCertificati(),
                p.getFoto()
        );
    }

    private BigDecimal safePrezzo(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private ItemType parseTipo(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return ItemType.valueOf(raw.toUpperCase(Locale.ITALY));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // ---------- view model ----------
    enum ItemType { PRODOTTO, PACCHETTO }

    public record ItemVM(
            ItemType tipo,
            String folder,            // "prodotti" | "pacchetti" (per costruire gli URL ai file)
            Long id,
            String nome,
            String descrizione,
            BigDecimal prezzo,
            String indirizzo,
            int quantita,
            String certificatiCsv,
            String fotoCsv
    ) {}
}
