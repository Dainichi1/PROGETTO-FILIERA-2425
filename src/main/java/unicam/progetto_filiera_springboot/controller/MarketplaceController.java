package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final ProdottoService prodottoService;

    @GetMapping
    public String index(@RequestParam(name = "id", required = false) Long id,
                        Model model) {

        List<ProdottoResponse> prodotti = prodottoService.listApprovati();
        model.addAttribute("items", prodotti); // lista a sinistra

        // dettaglio a destra (se ho un id, lo prendo dalla lista; altrimenti mostro empty-state)
        Optional<ProdottoResponse> selected = Optional.empty();
        if (id != null) {
            selected = prodotti.stream().filter(p -> p.getId().equals(id)).findFirst();
        }
        model.addAttribute("selected", selected.orElse(null));

        return "marketplace/index";
    }
}
