package unicam.filiera_agricola_2425.models;

import jakarta.persistence.Entity;

@Entity
public class Produttore extends Venditore {

    @Override
    protected String messaggioSpecifico() {
        return "puoi creare un prodotto e puoi inviare un nuovo prodotto al curatore.";
    }
}
