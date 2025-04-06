package unicam.filiera_agricola_2425.models;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Curatore extends UtenteAutenticato {

    @Override
    protected String messaggioSpecifico() {
        return "controlla i prodotti in attesa di approvazione.";
    }
}
