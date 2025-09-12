crudUtils.createInstance = function (cfg) {
    let localConfig = cfg;

    // --- live validation ---
    function attachLiveValidation() {
        if (!localConfig.formId) return;
        const form = document.getElementById(localConfig.formId);
        if (!form) return;
        form.querySelectorAll("input, textarea, select")
            .forEach(f => {
                f.addEventListener("input", () => formUtils.clearFieldError(f));
                f.addEventListener("change", () => formUtils.clearFieldError(f));
            });
    }

    function toggleForm() {
        const formContainer = document.getElementById(localConfig.formContainerId);
        const form = document.getElementById(localConfig.formId);
        const isHidden = formContainer.style.display === "none";

        formContainer.style.display = isHidden ? "block" : "none";

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

        // attacca validazione live ogni volta che mostri il form
        attachLiveValidation();
    }

    function togglePubblicati() {
        const container = document.getElementById("pubblicatiContainer");
        const btn = document.getElementById("togglePubblicatiBtn");
        if (!container || !btn) return;

        const isHidden = container.style.display === "none";
        container.style.display = isHidden ? "block" : "none";
        btn.textContent = isHidden ? "❌ Chiudi" : "📋 Visualizza visite/fiere pubblicate";
    }

    // bind evento al bottone (fallback nel caso defer impedisca onclick)
    document.addEventListener("DOMContentLoaded", () => {
        const btn = document.getElementById("togglePubblicatiBtn");
        if (btn) btn.addEventListener("click", togglePubblicati);
    });

    async function submitForm() {
        const ok = localConfig.validateFn ? localConfig.validateFn() : true;
        if (!ok) {
            if (localConfig.confirmModalId) modalUtils.closeModal(localConfig.confirmModalId);
            return;
        }

        if (localConfig.confirmModalId) modalUtils.closeModal(localConfig.confirmModalId);

        const form = document.getElementById(localConfig.formId);
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
                const isUpdate = form.action === localConfig.updateAction;
                if (isUpdate && typeof localConfig.onUpdateSuccess === "function") {
                    localConfig.onUpdateSuccess();
                } else if (!isUpdate && typeof localConfig.onCreateSuccess === "function") {
                    localConfig.onCreateSuccess();
                }
            } else {
                alert("Errore durante l'invio: " + (await res.text()));
            }
        } catch (e) {
            alert("Errore di rete durante l'invio: " + e.message);
        }
    }

    return {
        toggleForm,
        submitForm
    };
};
