/**
 * Trova lo span di errore collegato a un campo.
 */
function getErrorSpan(field) {
    let el = field.nextElementSibling;
    while (el && !el.classList.contains("error-message")) el = el.nextElementSibling;
    return el;
}

/**
 * Mostra un errore su un campo.
 */
function setFieldError(fieldOrId, message) {
    const field = typeof fieldOrId === "string" ? document.getElementById(fieldOrId) : fieldOrId;
    if (!field) return;

    field.classList.add("input-error"); // stile rosso per il campo
    const span = getErrorSpan(field);
    if (span) {
        span.textContent = message || "⚠ Campo obbligatorio";
    }
}

/**
 * Rimuove l'errore da un campo.
 */
function clearFieldError(fieldOrId) {
    const field = typeof fieldOrId === "string" ? document.getElementById(fieldOrId) : fieldOrId;
    if (!field) return;

    field.classList.remove("input-error");
    const span = getErrorSpan(field);
    if (span) {
        span.textContent = "";
    }
}

/**
 * Pulisce tutti gli errori di un form.
 */
function clearAllErrors(formId) {
    const form = document.getElementById(formId);
    if (!form) return;

    form.querySelectorAll(".input-error").forEach(el => el.classList.remove("input-error"));
    form.querySelectorAll(".error-message").forEach(span => {
        span.textContent = "";
    });
}

export const formUtils = {
    getErrorSpan,
    setFieldError,
    clearFieldError,
    clearAllErrors
};
