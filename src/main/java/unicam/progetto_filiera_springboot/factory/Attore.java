package unicam.progetto_filiera_springboot.factory;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public interface Attore {
    Ruolo getRuolo();

    String descrizione();
}
