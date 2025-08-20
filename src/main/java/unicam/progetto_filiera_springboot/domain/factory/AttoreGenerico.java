// Implementazioni "concrete product"
package unicam.progetto_filiera_springboot.domain.factory;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class AttoreGenerico implements Attore {
    private final Ruolo ruolo;

    public AttoreGenerico(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    @Override
    public Ruolo getRuolo() {
        return ruolo;
    }

    @Override
    public String descrizione() {
        return "Attore: " + ruolo.name();
    }
}
