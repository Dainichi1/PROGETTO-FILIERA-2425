import { modalUtils } from "./modal-utils.js";
import { formUtils } from "./form-utils.js";
import { csrfUtils } from "./csrf-utils.js";

export const recensioniUtils = (() => {
    let acquistoId = null;

    function openRecensioneModal(id) {
        acquistoId = id;
        resetRecensioneForm();
        modalUtils.openModal("recensionePostModal");
    }

    function resetRecensioneForm() {
        const titleEl = document.getElementById("recensioneTitle");
        const textEl = document.getElementById("recensioneText");

        if (titleEl) {
            titleEl.value = "";
            formUtils.clearFieldError(titleEl);
        }
        if (textEl) {
            textEl.value = "";
            formUtils.clearFieldError(textEl);
        }
    }

    function openRecensioneConfirm() {
        const titleEl = document.getElementById("recensioneTitle");
        const textEl = document.getElementById("recensioneText");

        let ok = true;
        formUtils.clearFieldError(titleEl);
        formUtils.clearFieldError(textEl);

        if (!titleEl.value.trim()) {
            formUtils.setFieldError("recensioneTitle", "⚠ Il titolo è obbligatorio");
            ok = false;
        }
        if (!textEl.value.trim()) {
            formUtils.setFieldError("recensioneText", "⚠ Il testo è obbligatorio");
            ok = false;
        }

        if (!ok) return;

        modalUtils.closeModal("recensionePostModal");
        modalUtils.openModal("recensioneConfirmModal");
    }

    async function submitRecensione() {
        modalUtils.closeModal("recensioneConfirmModal");

        const titolo = document.getElementById("recensioneTitle").value.trim();
        const testo = document.getElementById("recensioneText").value.trim();
        const { header, token } = csrfUtils.getCsrf();

        try {
            const res = await fetch(`/api/social/pubblica-recensione/${acquistoId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                },
                credentials: "same-origin",
                body: JSON.stringify({ titolo, testo })
            });

            if (res.ok) {
                resetRecensioneForm();
                modalUtils.openModal("recensioneSuccessModal");
            } else {
                const msg = await res.text();
                const errEl = document.getElementById("recensioneErrorMessage");
                if (errEl) errEl.innerText = msg;
                modalUtils.openModal("recensioneErrorModal");
            }
        } catch (e) {
            const errEl = document.getElementById("recensioneErrorMessage");
            if (errEl) errEl.innerText = "Errore di rete: " + e.message;
            modalUtils.openModal("recensioneErrorModal");
        }
    }

    function attachLiveValidation() {
        ["recensioneTitle", "recensioneText"].forEach(id => {
            const field = document.getElementById(id);
            if (field) {
                field.addEventListener("input", () => formUtils.clearFieldError(field));
                field.addEventListener("change", () => formUtils.clearFieldError(field));
            }
        });
    }

    return {
        openRecensioneModal,
        openRecensioneConfirm,
        submitRecensione,
        attachLiveValidation
    };
})();
