document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('toggleFormBtn');
    const card = document.getElementById('createProductCard');
    const form = document.getElementById('createProductForm');
    const modal = document.getElementById('confirmModal');
    const modalYes = document.getElementById('modalYes');
    const modalNo = document.getElementById('modalNo');
    const certificatiInput = document.getElementById('certificati');
    const certificatiInfo = document.getElementById('certificatiInfo');
    const fotoInput = document.getElementById('foto');
    const fotoInfo = document.getElementById('fotoInfo');

    const updateFileInfo = (input, infoEl) => {
        infoEl.textContent = input.files.length > 0
            ? Array.from(input.files).map(f => f.name).join(', ')
            : "Nessun file selezionato";
    };

    certificatiInput?.addEventListener('change', () => updateFileInfo(certificatiInput, certificatiInfo));
    fotoInput?.addEventListener('change', () => updateFileInfo(fotoInput, fotoInfo));

    toggleBtn?.addEventListener('click', () => {
        const visible = card.style.display !== 'none';
        card.style.display = visible ? 'none' : 'block';
        toggleBtn.textContent = visible ? 'Crea Prodotto' : 'Chiudi Form';
    });

    form?.addEventListener('submit', e => {
        if (form.dataset.confirmed === "true") return;
        e.preventDefault();
        modal?.classList.add('open');
    });

    modalYes?.addEventListener('click', () => {
        modal.classList.remove('open');
        form.dataset.confirmed = "true";
        form.submit();
    });

    modalNo?.addEventListener('click', () => {
        modal.classList.remove('open');
        window.location.href = '/produttore';
    });
});
