const crudUtils = (() => {
    let config = {};

    function init(cfg) {
        config = cfg;
        attachLiveValidation(cfg.formId);
        attachSocialLiveValidation();
    }

    // ===== SOCIAL =====
    function handleSocialClick(button) {
        let stato = button.getAttribute("data-stato") || "";
        const type = button.getAttribute("data-type");

        stato = stato.trim().toUpperCase();

        if (stato === "IN_ATTESA" || stato === "RIFIUTATO") {
            const msg = `L'opzione è disponibile solo per ${type} approvati.`;
            document.getElementById("socialNotAvailableMessage").innerText = msg;
            modalUtils.openModal("socialNotAvailableModal");
            return;
        }

        if (stato === "APPROVATO") {
            config.currentItemId = button.getAttribute("data-id");
            modalUtils.openModal("socialPostModal");
        }
    }

    function openSocialConfirm() {
        const titleEl = document.getElementById("postTitle");
        const textEl = document.getElementById("postText");

        let ok = true;
        formUtils.clearFieldError(titleEl);
        formUtils.clearFieldError(textEl);

        if (!titleEl.value.trim()) {
            formUtils.setFieldError("postTitle", "⚠ Il titolo è obbligatorio");
            ok = false;
        }
        if (!textEl.value.trim()) {
            formUtils.setFieldError("postText", "⚠ Il testo è obbligatorio");
            ok = false;
        }

        if (!ok) return;

        modalUtils.closeModal("socialPostModal");
        modalUtils.openModal("socialConfirmModal");
    }

    async function submitSocialPost() {
        modalUtils.closeModal("socialConfirmModal");

        const title = document.getElementById("postTitle").value.trim();
        const text = document.getElementById("postText").value.trim();
        const { header, token } = getCsrf();
        const tipo = config.itemType;

        try {
            const res = await fetch(`/api/social/pubblica/${config.currentItemId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                },
                credentials: "same-origin",
                body: JSON.stringify({ titolo: title, testo: text, tipoItem: tipo })
            });

            if (res.ok) {
                modalUtils.openModal("socialSuccessModal");
            } else {
                const msg = await res.text();
                alert("Errore durante la pubblicazione: " + msg);
            }
        } catch (e) {
            alert("Errore di rete: " + e.message);
        }
    }

    // ===== TOGGLE FORM =====
    function toggleForm() {
        const formContainer = document.getElementById(config.formContainerId);
        const form = document.getElementById(config.formId);
        const isHidden = formContainer.style.display === "none";
        formContainer.style.display = isHidden ? "block" : "none";

        if (!isHidden) {
            document.getElementById(config.formTitleId).textContent = config.createTitle;
            form.reset();
            form.setAttribute("action", config.createAction);
            document.getElementById(config.itemIdField).value = "";
            document.getElementById(config.itemTypeField).value = config.itemType;
            document.getElementById(config.createButtonsId).style.display = "block";
            document.getElementById(config.updateButtonsId).style.display = "none";
            if (typeof clearAllErrors === "function") clearAllErrors();
        }
    }

    // ===== SUBMIT FORM =====
    async function submitForm() {
        const ok = config.validateFn ? config.validateFn() : true;
        if (!ok) {
            modalUtils.closeModal("createConfirmModal");
            modalUtils.closeModal("updateConfirmModal");
            return;
        }

        modalUtils.closeModal("createConfirmModal");
        modalUtils.closeModal("updateConfirmModal");

        const form = document.getElementById(config.formId);
        const formData = new FormData(form);
        const { header, token } = getCsrf();

        try {
            const res = await fetch(form.action, {
                method: form.method || "POST",
                body: formData,
                headers: { [header]: token },
                credentials: "same-origin"
            });

            if (res.ok) {
                const isUpdate = form.action === config.updateAction;
                const modalId = isUpdate ? "updateSuccessModal" : "createSuccessModal";

                if (isUpdate && typeof config.onUpdateSuccess === "function") {
                    config.onUpdateSuccess();
                } else if (!isUpdate && typeof config.onCreateSuccess === "function") {
                    config.onCreateSuccess();
                } else {
                    const msg = `${config.labels.itemName} inviato al Curatore con successo!`;
                    const span = document.querySelector(`#${modalId} p`);
                    if (span) span.textContent = msg;
                    modalUtils.openModal(modalId);
                }
            } else {
                alert("Errore durante l'invio: " + (await res.text()));
            }
        } catch (e) {
            alert("Errore di rete durante l'invio: " + e.message);
        }
    }

    // ===== DELETE =====
    let toDelete = { id: null, nome: null };

    function handleDeleteClick(button) {
        const stato = button.getAttribute("data-stato");
        const id = button.getAttribute("data-id");
        const nome = button.getAttribute("data-nome");

        if (!config.deletableStates.includes(stato)) {
            modalUtils.openModal(config.deleteErrorModalId);
            return;
        }

        toDelete = { id, nome };
        document.getElementById(config.deleteConfirmMessageId).textContent =
            `Eliminare "${nome}"?`;
        modalUtils.openModal(config.deleteConfirmModalId);
    }

    async function confirmDelete() {
        modalUtils.closeModal(config.deleteConfirmModalId);

        // Recupera CSRF da csrfUtils
        const { header, token } = csrfUtils.getCsrf();

        try {
            const res = await fetch(config.deleteUrl(toDelete.id), {
                method: "DELETE",
                headers: { [header]: token },
                credentials: "same-origin"
            });

            const text = await res.text();
            if (res.ok) {
                // Rimuove la riga dalla tabella
                document.querySelector(`tr[data-id="${toDelete.id}"]`)?.remove();

                document.getElementById(config.deleteSuccessMessageId).textContent =
                    text || `${config.labels.itemName} "${toDelete.nome}" eliminato con successo`;

                modalUtils.openModal(config.deleteSuccessModalId);
            } else {
                document.getElementById(config.deleteGenericErrorMessageId).textContent =
                    text || "Errore durante l'eliminazione.";
                modalUtils.openModal(config.deleteGenericErrorModalId);
            }
        } catch {
            document.getElementById(config.deleteGenericErrorMessageId).textContent =
                "Errore di rete durante l'eliminazione.";
            modalUtils.openModal(config.deleteGenericErrorModalId);
        }
    }

    // ===== EDIT =====
    async function handleEditClick(button) {
        const stato = button.getAttribute("data-stato");
        const id = button.getAttribute("data-id");

        if (stato !== config.editableState) {
            modalUtils.openModal(config.editErrorModalId);
            return;
        }

        const form = document.getElementById(config.formId);
        if (stato === "RIFIUTATO") {
            form.setAttribute("action", config.updateRejectedAction);
        } else {
            form.setAttribute("action", config.updateAction);
        }

        setUpdateModeForm(id);
        if (typeof clearAllErrors === "function") clearAllErrors();

        try {
            const res = await fetch(config.fetchUrl(id), { credentials: "same-origin" });
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            config.prefillFormFn(data);
        } catch (e) {
            alert("Impossibile caricare: " + (e.message || "errore"));
        }
    }

    // ===== SOCIAL FEED =====
    async function openSocialFeed() {
        try {
            const res = await fetch("/api/social", { credentials: "same-origin" });
            if (!res.ok) throw new Error("Errore nel recupero dei post social");

            const posts = await res.json();
            const container = document.getElementById("socialPostsContainer");
            container.innerHTML = "";

            if (posts.length === 0) {
                container.innerHTML = "<p>Nessun post pubblicato.</p>";
            } else {
                posts.forEach(post => {
                    const div = document.createElement("div");
                    div.classList.add("social-post");
                    div.style.border = "1px solid #ccc";
                    div.style.padding = "10px";
                    div.style.marginBottom = "10px";
                    div.style.borderRadius = "8px";
                    div.style.background = "#fff";
                    div.innerHTML = `
                        <h4>${post.titolo}</h4>
                        <p>${post.testo}</p>
                        <small>
                            👤 ${post.autoreUsername} |
                            📦 ${post.tipoItem}: ${post.nomeItem} |
                            🕒 ${new Date(post.createdAt).toLocaleString()}
                        </small>
                    `;
                    container.appendChild(div);
                });
            }

            modalUtils.openModal("socialFeedModal");
        } catch (e) {
            alert("Errore durante il caricamento dei post: " + e.message);
        }
    }

    function setUpdateModeForm(id) {
        document.getElementById(config.formTitleId).textContent = config.updateTitle;
        const form = document.getElementById(config.formId);
        form.setAttribute("action", config.updateAction);
        document.getElementById(config.itemIdField).value = id || "";
        document.getElementById(config.itemTypeField).value = config.itemType;
        document.getElementById(config.createButtonsId).style.display = "none";
        document.getElementById(config.updateButtonsId).style.display = "block";
        document.getElementById(config.formContainerId).style.display = "block";
    }

    // ===== UTILS =====
    function attachLiveValidation(formId) {
        if (!formId) return;
        const form = document.getElementById(formId);
        if (!form) return;
        form.querySelectorAll("input, textarea, select").forEach(f => {
            f.addEventListener("input", () => formUtils.clearFieldError(f));
            f.addEventListener("change", () => formUtils.clearFieldError(f));
        });
    }

    function attachSocialLiveValidation() {
        ["postTitle", "postText"].forEach(id => {
            const field = document.getElementById(id);
            if (field) {
                field.addEventListener("input", () => formUtils.clearFieldError(field));
                field.addEventListener("change", () => formUtils.clearFieldError(field));
            }
        });
    }

    // ===== CREATE INSTANCE (per fiere/visite) =====
    function createInstance(cfg) {
        const instance = { ...crudUtils, config: cfg };
        document.addEventListener("DOMContentLoaded", () => {
            attachLiveValidation(cfg.formId);
            attachSocialLiveValidation();
        });
        return instance;
    }

    // ===== FIX REDIRECT BUTTON =====
    document.addEventListener("click", e => {
        if (e.target && e.target.classList.contains("redirect-btn")) {
            modalUtils.closeModal("createSuccessModal");
            modalUtils.closeModal("updateSuccessModal");

            const url = e.target.getAttribute("data-redirect");
            if (url) window.location.href = url;
        }
    });

    // ===== PRENOTAZIONE VISITE =====
    function openPrenotazioneVisitaModal(button) {
        const visitaId = button.getAttribute("data-id");
        document.getElementById("idVisitaPrenotazione").value = visitaId;
        modalUtils.openModal("prenotazioneVisitaModal");
    }

    function attachPrenotazioneVisitaValidation() {
        const form = document.getElementById("prenotazioneVisitaForm");
        if (!form) return;

        form.setAttribute("novalidate", "true"); // disattiva messaggi standard del browser

        form.addEventListener("submit", (e) => {
            const input = document.getElementById("numeroPersone");
            const errorSpan = document.getElementById("numeroPersoneError");

            if (errorSpan) errorSpan.textContent = ""; // reset messaggi

            if (!input.value || parseInt(input.value) < 1) {
                e.preventDefault(); // blocca invio
                if (errorSpan) {
                    errorSpan.textContent = "⚠ Devi inserire almeno 1 persona";
                } else {
                    alert("⚠ Devi inserire almeno 1 persona");
                }
            }
        });
    }


    return {
        init,
        createInstance,
        toggleForm,
        submitForm,
        handleDeleteClick,
        confirmDelete,
        handleEditClick,
        handleSocialClick,
        openSocialConfirm,
        submitSocialPost,
        openSocialFeed,
        openPrenotazioneVisitaModal,
        attachPrenotazioneVisitaValidation
    };
})();
