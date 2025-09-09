package unicam.filiera.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.*;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.*;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.ProdottoTrasformato;

import java.util.List;
import java.util.Set;


@Controller
@RequestMapping("/venditore/item")
public class ItemController {

    private final ItemService itemService;
    private final Validator validator;

    // Inject per popolare le liste in dashboard
    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;
    private final UtenteService utenteService;

    @Autowired
    public ItemController(ItemService itemService,
                          Validator validator,
                          ProdottoService prodottoService,
                          PacchettoService pacchettoService,
                          ProdottoTrasformatoService trasformatoService,
                          UtenteService utenteService){
        this.itemService = itemService;
        this.validator = validator;
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
        this.utenteService = utenteService;
    }

    @PostMapping("/modifica")
    public String modificaItemRifiutato(
            @RequestParam("tipo") ItemTipo tipo,
            @RequestParam(value = "id", required = false) Long id,
            HttpServletRequest request,
            Authentication auth,
            RedirectAttributes ra,
            Model model
    ) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        if (id == null) {
            ra.addFlashAttribute("updateErrorMessage", "ID mancante per la modifica");
            return "redirect:" + redirectFor(tipo);
        }
        // 1) DTO dinamico
        BaseItemDto dto = newDtoFor(tipo);

        // 2) Binding request params
        WebDataBinder binder = new WebDataBinder(dto);
        PropertyValues pvs = new MutablePropertyValues(request.getParameterMap());
        binder.bind(pvs);

        // 3) Gestione multipart (certificati, foto)
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            PropertyAccessorFactory.forBeanPropertyAccess(dto)
                    .setPropertyValue("certificati", multipartRequest.getFiles("certificati"));
            PropertyAccessorFactory.forBeanPropertyAccess(dto)
                    .setPropertyValue("foto", multipartRequest.getFiles("foto"));
        }

        dto.setTipo(tipo);
        dto.setId(id);

        // 4) Validazione JSR-380 -> aggancio al giusto oggetto di form
        String attrName = attrNameFor(tipo); // es. "prodottoDto"
        BindingResult br = new BeanPropertyBindingResult(dto, attrName);

        Set<ConstraintViolation<BaseItemDto>> violations = validator.validate(dto);
        for (ConstraintViolation<BaseItemDto> v : violations) {
            String field = (v.getPropertyPath() != null) ? v.getPropertyPath().toString() : null;
            if (field != null && !field.isBlank()) {
                br.addError(new FieldError(attrName, field, v.getMessage()));
            }
        }

        switch (tipo) {
            case PRODOTTO -> validaProdotto((ProdottoDto) dto, br);
            case PACCHETTO -> validaPacchetto((PacchettoDto) dto, br);
            case TRASFORMATO -> validaTrasformato((ProdottoTrasformatoDto) dto, br);
        }

        // 6) Se ci sono errori: restituisci la DASHBOARD del tipo con form aperto in update
        if (br.hasErrors()) {
            // dto + bindingresult per Thymeleaf
            model.addAttribute(attrName, dto);
            model.addAttribute("org.springframework.validation.BindingResult." + attrName, br);

            // carica la lista per la tabella (adatta i nomi se la tua view usa altri attributi)
            loadDashboardLists(model, tipo, username);

            // flag per far aprire il form in modalità update lato view
            model.addAttribute("showForm", true);
            model.addAttribute("updateMode", true);

            return viewFor(tipo); // es. "dashboard/produttore"
        }

        // 7) Business logic
        try {
            itemService.modificaRifiutato(dto, username);

            String tipoLabel = switch (tipo) {
                case PRODOTTO -> "Prodotto";
                case PACCHETTO -> "Pacchetto";
                case TRASFORMATO -> "Prodotto trasformato";
            };
            ra.addFlashAttribute("updateSuccessMessage", tipoLabel + " aggiornato e reinviato al Curatore");

        } catch (Exception e) {
            br.reject("update.error", (e.getMessage() == null ? "Errore durante l'aggiornamento" : e.getMessage()));
            model.addAttribute(attrName, dto);
            model.addAttribute("org.springframework.validation.BindingResult." + attrName, br);
            loadDashboardLists(model, tipo, username);
            model.addAttribute("showForm", true);
            model.addAttribute("updateMode", true);
            return viewFor(tipo);
        }

        return "redirect:" + redirectFor(tipo);
    }

    // --- Helpers ---

    private BaseItemDto newDtoFor(ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> new ProdottoDto();
            case PACCHETTO -> new PacchettoDto();
            case TRASFORMATO -> new ProdottoTrasformatoDto();
        };
    }

    private String redirectFor(ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> "/produttore/dashboard";
            case PACCHETTO -> "/distributore/dashboard";
            case TRASFORMATO -> "/trasformatore/dashboard";
        };
    }

    private String viewFor(ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> "dashboard/produttore";
            case PACCHETTO -> "dashboard/distributore";
            case TRASFORMATO -> "dashboard/trasformatore";
        };
    }

    private String attrNameFor(ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> "prodottoDto";
            case PACCHETTO -> "pacchettoDto";
            case TRASFORMATO -> "trasformatoDto";
        };
    }

    private void loadDashboardLists(Model model, ItemTipo tipo, String username) {
        switch (tipo) {
            case PRODOTTO -> {
                List<Prodotto> prodotti = prodottoService.getProdottiCreatiDa(username);
                model.addAttribute("prodotti", prodotti);
            }
            case PACCHETTO -> {
                List<Pacchetto> pacchetti = pacchettoService.getPacchettiCreatiDa(username);
                model.addAttribute("pacchetti", pacchetti);
                model.addAttribute("prodottiApprovati",
                        prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            }
            case TRASFORMATO -> {
                List<ProdottoTrasformato> trasformati =
                        trasformatoService.getProdottiTrasformatiCreatiDa(username);
                model.addAttribute("trasformati", trasformati);

                // allineiamo agli attributi attesi dalla view 'dashboard/trasformatore'
                model.addAttribute("prodottiApprovati",
                        prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
                model.addAttribute("produttori", utenteService.getProduttori());
            }
        }
    }

    // --- Validazioni centralizzate  ---

    private void validaProdotto(ProdottoDto dto, BindingResult br) {
        boolean isCreazione = (dto.getId() == null);
        if (isCreazione) {
            if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                    || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (dto.getFoto() == null || dto.getFoto().isEmpty()
                    || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }
    }

    private void validaPacchetto(PacchettoDto dto, BindingResult br) {
        boolean isCreazione = (dto.getId() == null);
        if (isCreazione) {
            if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                    || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (dto.getFoto() == null || dto.getFoto().isEmpty()
                    || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }
        if (dto.getProdottiSelezionati() == null || dto.getProdottiSelezionati().size() < 2) {
            br.rejectValue("prodottiSelezionati", "error.prodottiSelezionati",
                    "Devi selezionare almeno 2 prodotti per creare il pacchetto");
        }
    }

    private void validaTrasformato(ProdottoTrasformatoDto dto, BindingResult br) {
        boolean isCreazione = (dto.getId() == null);
        if (isCreazione) {
            if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                    || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (dto.getFoto() == null || dto.getFoto().isEmpty()
                    || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }
        if (dto.getFasiProduzione() == null ||
                dto.getFasiProduzione().stream().filter(f ->
                        f != null &&
                                f.getDescrizioneFase()!=null && !f.getDescrizioneFase().isBlank() &&
                                f.getProduttoreUsername()!=null && !f.getProduttoreUsername().isBlank() &&
                                f.getProdottoOrigineId()!=null
                ).count() < 2) {
            br.rejectValue("fasiProduzione", "error.fasiProduzione", "Devi inserire almeno 2 fasi di produzione");
        }
    }

}
