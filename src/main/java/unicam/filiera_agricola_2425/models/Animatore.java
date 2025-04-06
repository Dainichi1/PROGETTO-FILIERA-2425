package unicam.filiera_agricola_2425.models;



import jakarta.persistence.Entity;

@Entity
public class Animatore extends UtenteAutenticato {

    @Override
    protected String messaggioSpecifico() {
        return "puoi inviare un nuovo prodotto al curatore.";
    }
}

