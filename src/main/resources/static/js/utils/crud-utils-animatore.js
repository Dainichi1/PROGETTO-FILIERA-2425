crudUtils.createInstance = function (cfg) {
    let localConfig = cfg;

    console.log("[INIT] Nuova istanza CRUD creata con config:", localConfig);

    // === LIVE VALIDATION UNIVERSALE ===
    function attachLiveValidation(formId) {
        if (!formId) return;
        const form = document.getElementById(formId);
        if (!form) {
            console.warn("[DEBUG] Nessun form trovato per:", formId);
            return;
        }

        // input, textarea, select
        form.querySelectorAll("input, textarea, select").forEach(f => {
            f.addEventListener("input", () => {
                formUtils.clearFieldError(f.id);   // usa l'ID
            });
            f.addEventListener("change", () => {
                formUtils.clearFieldError(f.id);
            });
        });

        // checkbox e radio (es. destinatari)
        form.querySelectorAll("input[type=checkbox], input[type=radio]").forEach(cb => {
            cb.addEventListener("change", () => {
                formUtils.clearFieldError(cb.name); // pulisci eventuali errori per il gruppo
                formUtils.clearFieldError("destinatari-container"); // forza il clear sul container
            });
        });

        console.log("[DEBUG] Live validation attaccata al form:", formId);
    }


    // === TOGGLE FORM ===
    function toggleForm() {
        const formContainer = document.getElementById(localConfig.formContainerId);
        const form = document.getElementById(localConfig.formId);
        if (!formContainer || !form) {
            console.error("[DEBUG] toggleForm: form o container non trovato!");
            return;
        }

        const isHidden = formContainer.style.display === "none";
        formContainer.style.display = isHidden ? "block" : "none";

        console.log("[DEBUG] toggleForm:", {
            formId: localConfig.formId,
            visibile: isHidden
        });

        if (!isHidden) {
            document.getElementById(localConfig.formTitleId).textContent = localConfig.createTitle;
            form.reset();
            form.setAttribute("action", localConfig.createAction);
            document.getElementById(localConfig.itemIdField).value = "";
            document.getElementById(localConfig.itemTypeField).value = localConfig.itemType;
            document.getElementById(localConfig.createButtonsId).style.display = "block";
            document.getElementById(localConfig.updateButtonsId).style.display = "none";
            if (typeof clearAllErrors === "function") clearAllErrors();
        }

        attachLiveValidation(localConfig.formId);
    }

    // === TOGGLE PUBBLICATI ===
    function togglePubblicati() {
        const container = document.getElementById("pubblicatiContainer");
        const btn = document.getElementById("togglePubblicatiBtn");
        if (!container || !btn) {
            console.error("[DEBUG] togglePubblicati: container o bottone non trovato");
            return;
        }

        const isHidden = container.style.display === "none";
        container.style.display = isHidden ? "block" : "none";
        btn.textContent = isHidden ? "❌ Chiudi" : "📋 Visualizza visite/fiere pubblicate";

        console.log("[DEBUG] togglePubblicati -> visibile:", isHidden);
    }

    document.addEventListener("DOMContentLoaded", () => {
        const btn = document.getElementById("togglePubblicatiBtn");
        if (btn) {
            console.log("[DEBUG] Agganciato listener a togglePubblicatiBtn");
            btn.addEventListener("click", togglePubblicati);
        }
        attachLiveValidation(cfg.formId);
    });

    // === SUBMIT FORM ===
    async function submitForm() {
        console.log("[DEBUG] submitForm avviato per:", localConfig.itemType);

        const ok = localConfig.validateFn ? localConfig.validateFn() : true;
        if (!ok) {
            console.warn("[DEBUG] Validazione fallita per:", localConfig.itemType);
            if (localConfig.confirmModalId) modalUtils.closeModal(localConfig.confirmModalId);
            return;
        }

        if (localConfig.confirmModalId) modalUtils.closeModal(localConfig.confirmModalId);

        const form = document.getElementById(localConfig.formId);
        const formData = new FormData(form);
        const { header, token } = getCsrf();

        console.log("[DEBUG] Invio form:", {
            action: form.action,
            method: form.method,
            dati: Object.fromEntries(formData)
        });

        try {
            const res = await fetch(form.action, {
                method: form.method || "POST",
                body: formData,
                headers: { [header]: token },
                credentials: "same-origin"
            });

            if (res.ok) {
                console.log("[DEBUG] submitForm OK:", localConfig.itemType);
                const isUpdate = form.action === localConfig.updateAction;
                if (isUpdate && typeof localConfig.onUpdateSuccess === "function") {
                    localConfig.onUpdateSuccess();
                } else if (!isUpdate && typeof localConfig.onCreateSuccess === "function") {
                    localConfig.onCreateSuccess();
                } else {
                    modalUtils.openModal("genericSuccessModal");
                }
            } else {
                const msg = await res.text();
                console.error("[DEBUG] submitForm errore HTTP:", res.status, msg);
                alert("Errore durante l'invio: " + msg);
            }
        } catch (e) {
            console.error("[DEBUG] submitForm errore di rete:", e);
            alert("Errore di rete durante l'invio: " + e.message);
        }
    }

    // === SOCIAL POST ===
    function openSocialModal(id, type) {
        console.log("[DEBUG] openSocialModal chiamata con:", { id, type });
        localConfig.currentItemId = id;
        localConfig.currentItemType = type;

        console.log("[DEBUG] Stato aggiornato:", {
            currentItemId: localConfig.currentItemId,
            currentItemType: localConfig.currentItemType
        });

        window.currentCrud = instance;
        modalUtils.openModal(localConfig.socialPostModalId || "socialPostModal");
    }

    function openSocialConfirm() {
        const titleEl = document.getElementById(localConfig.postTitleId || "postTitle");
        const textEl = document.getElementById(localConfig.postTextId || "postText");

        let ok = true;
        formUtils.clearFieldError(titleEl);
        formUtils.clearFieldError(textEl);

        if (!titleEl.value.trim()) {
            formUtils.setFieldError(titleEl.id, "⚠ Il titolo è obbligatorio");
            ok = false;
        }
        if (!textEl.value.trim()) {
            formUtils.setFieldError(textEl.id, "⚠ Il testo è obbligatorio");
            ok = false;
        }

        console.log("[DEBUG] openSocialConfirm -> titolo:", titleEl.value, "testo:", textEl.value, "ok:", ok);

        if (!ok) return;
        modalUtils.closeModal(localConfig.socialPostModalId || "socialPostModal");
        modalUtils.openModal(localConfig.socialConfirmModalId || "socialConfirmModal");
    }

    async function submitSocialPost() {
        modalUtils.closeModal(localConfig.socialConfirmModalId || "socialConfirmModal");

        const titolo = document.getElementById(localConfig.postTitleId || "postTitle").value.trim();
        const testo = document.getElementById(localConfig.postTextId || "postText").value.trim();
        const { header, token } = getCsrf();

        console.log("[DEBUG] submitSocialPost -> preparo richiesta", {
            id: localConfig.currentItemId,
            type: localConfig.currentItemType,
            titolo,
            testo
        });

        if (!localConfig.currentItemId) {
            console.error("[ERRORE] Nessun currentItemId trovato!");
            alert("Errore interno: nessun ID associato all'elemento da pubblicare.");
            return;
        }

        try {
            const res = await fetch(`/api/social/pubblica/${localConfig.currentItemId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                },
                body: JSON.stringify({ titolo, testo, tipoItem: localConfig.currentItemType })
            });

            if (res.ok) {
                console.log("[DEBUG] submitSocialPost SUCCESS:", {
                    id: localConfig.currentItemId,
                    type: localConfig.currentItemType
                });
                modalUtils.openModal(localConfig.socialSuccessModalId || "socialSuccessModal");
            } else {
                const msg = await res.text();
                console.error("[DEBUG] submitSocialPost ERRORE HTTP:", res.status, msg);
                alert("Errore: " + msg);
            }
        } catch (e) {
            console.error("[DEBUG] submitSocialPost ERRORE DI RETE:", e);
            alert("Errore di rete: " + e.message);
        }
    }

    // === ISTANZA ===
    const instance = {
        toggleForm,
        submitForm,
        togglePubblicati,
        openSocialModal,
        openSocialConfirm,
        submitSocialPost
    };

    return instance;
};
