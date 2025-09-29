import { modalUtils } from "./modal-utils.js";
import { formUtils } from "./form-utils.js";
import { csrfUtils } from "./csrf-utils.js";

/**
 * Utility dedicate alla gestione SOCIAL (post, conferme, feed)
 */
export const socialUtilsCrud = (() => {
    let config = {};

    function init(cfg) {
        config = cfg;
        attachSocialLiveValidation();
    }

    // ===== RESET FORM SOCIAL =====
    function resetSocialForm() {
        const titleEl = document.getElementById("postTitle");
        const textEl = document.getElementById("postText");

        if (titleEl) {
            titleEl.value = "";
            formUtils.clearFieldError(titleEl);
        }
        if (textEl) {
            textEl.value = "";
            formUtils.clearFieldError(textEl);
        }
    }

    // ===== CLICK PULSANTE SOCIAL =====
    // ===== CLICK PULSANTE SOCIAL =====
    function handleSocialClick(button) {
        const stato = (button.getAttribute("data-stato") || "").trim().toUpperCase();
        const type = button.getAttribute("data-type");
        const id = button.getAttribute("data-id");

        if (stato !== "APPROVATO") {
            // Messaggio uniforme per IN_ATTESA o RIFIUTATO
            const msg = `⚠ Puoi pubblicare solo ${type} con stato APPROVATO.`;
            const msgEl = document.getElementById("socialNotAvailableMessage");
            if (msgEl) msgEl.innerText = msg;
            modalUtils.openModal("socialNotAvailableModal");
            return;
        }

        // Stato APPROVATO → apri la modale social
        openSocialModal(id, type);
    }

    // ===== CONFERMA SOCIAL POST =====
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

    // ===== INVIO SOCIAL POST =====
    async function submitSocialPost() {
        modalUtils.closeModal("socialConfirmModal");

        const title = document.getElementById("postTitle").value.trim();
        const text = document.getElementById("postText").value.trim();
        const { header, token } = csrfUtils.getCsrf();
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
                resetSocialForm(); // pulizia automatica dopo successo
                modalUtils.openModal("socialSuccessModal");
            } else {
                const msg = await res.text();
                alert("Errore durante la pubblicazione: " + msg);
            }
        } catch (e) {
            alert("Errore di rete: " + e.message);
        }
    }

    // ===== SOCIAL FEED =====
    async function openSocialFeed() {
        const container = document.getElementById("socialPostsContainer");
        container.innerHTML = ""; // sempre svuotato

        try {
            const res = await fetch("/api/social", { credentials: "same-origin" });

            if (!res.ok) {
                throw new Error("Errore nel recupero dei post social");
            }

            const posts = await res.json();

            if (!posts || posts.length === 0) {
                container.innerHTML = "<p>⚠️ Nessun post pubblicato.</p>";
            } else {
                posts.forEach(post => {
                    const div = document.createElement("div");
                    div.classList.add("social-post");
                    div.style.cssText = `
                    border:1px solid #ccc;
                    padding:10px;
                    margin-bottom:10px;
                    border-radius:8px;
                    background:#fff;
                `;
                    div.innerHTML = `
                    <h4>${post.titolo}</h4>
                    <p>${post.testo}</p>
                    <small>
                        👤 ${post.autoreUsername || "Utente"} |
                        📦 ${post.tipoItem || "Item"}: ${post.nomeItem || "-"} |
                        🕒 ${post.createdAt ? new Date(post.createdAt).toLocaleString() : ""}
                    </small>
                `;
                    container.appendChild(div);
                });
            }
        } catch (e) {
            console.error("Errore durante il caricamento dei post:", e);
            container.innerHTML = "<p>❌ Errore durante il caricamento dei post.</p>";
        }

        // Apri sempre la modale, anche in caso di errore o lista vuota
        modalUtils.openModal("socialFeedModal");
    }


    // ===== APRI MODALE SOCIAL DIRETTO =====
    function openSocialModal(id, type) {
        config.currentItemId = id;
        config.itemType = type;

        resetSocialForm(); // pulizia campi
        modalUtils.openModal("socialPostModal");
    }

    // ===== VALIDAZIONE LIVE =====
    function attachSocialLiveValidation() {
        ["postTitle", "postText"].forEach(id => {
            const field = document.getElementById(id);
            if (field) {
                field.addEventListener("input", () => formUtils.clearFieldError(field));
                field.addEventListener("change", () => formUtils.clearFieldError(field));
            }
        });
    }

    return {
        init,
        handleSocialClick,
        openSocialConfirm,
        submitSocialPost,
        openSocialFeed,
        openSocialModal
    };
})();
