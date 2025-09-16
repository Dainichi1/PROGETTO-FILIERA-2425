package unicam.filiera.controller.base;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.BaseItemDto;

import java.util.function.BiConsumer;

public abstract class AbstractCreationController<T extends BaseItemDto> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    // ========== Abstract methods da implementare nei figli ==========

    protected abstract T newDto();

    protected abstract String getDtoName();

    protected abstract String getViewName();

    protected abstract String getRedirectPath();

    protected abstract void loadDashboardLists(Model model, String username);

    protected abstract void doCreate(T dto, String username) throws Exception;

    protected abstract String getSuccessMessage();

    protected abstract void doDelete(Long id, String username) throws Exception;

    // ========== Utility comuni ==========

    protected String resolveUsername(Authentication auth) {
        return (auth != null) ? auth.getName() : "demo_user";
    }

    protected String buildSuccessMessage(String action, String itemName) {
        return itemName + " " + action + " con successo";
    }

    protected String buildErrorMessage(String action, String itemName, Exception ex) {
        return "Errore durante " + action + " " + itemName + ": " + ex.getMessage();
    }

    // ========== Gestione DTO di default ==========
    @ModelAttribute
    public void addDefaultDto(Model model) {
        if (!model.containsAttribute(getDtoName())) {
            model.addAttribute(getDtoName(), newDto());
        }
    }

    // ========== Template Methods comuni ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        return showDashboard(model, auth);
    }

    public String showDashboard(Model model, Authentication auth) {
        String username = resolveUsername(auth);
        loadDashboardLists(model, username);
        model.addAttribute("showForm", false);
        model.addAttribute("redirectPath", getRedirectPath());
        return getViewName();
    }

    @PostMapping("/crea")
    public String crea(@Valid @ModelAttribute T dto,
                       BindingResult bindingResult,
                       Authentication auth,
                       RedirectAttributes redirectAttrs,
                       Model model) {
        return createItem(dto, bindingResult, auth, redirectAttrs, model);
    }

    public String createItem(@Valid @ModelAttribute T dto,
                             BindingResult bindingResult,
                             Authentication auth,
                             RedirectAttributes redirectAttrs,
                             Model model) {
        String username = resolveUsername(auth);

        if (bindingResult.hasErrors()) {
            log.warn("Creazione fallita per errori di validazione");
            loadDashboardLists(model, username);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return getViewName();
        }

        try {
            doCreate(dto, username);
            redirectAttrs.addFlashAttribute("createSuccessMessage", getSuccessMessage());
            return "redirect:" + getRedirectPath();
        } catch (Exception ex) {
            log.error("Errore nella creazione", ex);
            loadDashboardLists(model, username);
            model.addAttribute("errorMessage", buildErrorMessage("creazione", getDtoName(), ex));
            model.addAttribute("showForm", true);
            return getViewName();
        }
    }

    public String updateItem(@Valid @ModelAttribute T dto,
                             BindingResult bindingResult,
                             Authentication auth,
                             RedirectAttributes redirectAttrs,
                             Model model,
                             BiConsumer<T, String> updateFunction) {
        String username = resolveUsername(auth);

        if (bindingResult.hasErrors()) {
            log.warn("Update fallito per errori di validazione");
            loadDashboardLists(model, username);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return getViewName();
        }

        try {
            updateFunction.accept(dto, username);
            redirectAttrs.addFlashAttribute("updateSuccessMessage",
                    buildSuccessMessage("modificato", getDtoName()));
            return "redirect:" + getRedirectPath();
        } catch (Exception ex) {
            log.error("Errore durante update", ex);
            loadDashboardLists(model, username);
            model.addAttribute("errorMessage", buildErrorMessage("modifica", getDtoName(), ex));
            model.addAttribute("showForm", true);
            return getViewName();
        }
    }

    public ResponseEntity<String> deleteItem(@PathVariable Long id, Authentication auth) {
        String username = resolveUsername(auth);
        try {
            doDelete(id, username);
            return ResponseEntity.ok(buildSuccessMessage("eliminato", getDtoName()));
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest().body(ise.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Errore durante eliminazione", e);
            return ResponseEntity.internalServerError()
                    .body(buildErrorMessage("eliminazione", getDtoName(), e));
        }
    }
}
