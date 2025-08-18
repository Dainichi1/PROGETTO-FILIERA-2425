package unicam.progetto_filiera_springboot.domain.actor;

public class UtenteGenerico implements Utente {
    @Override public String getUsername() { return ""; }
    @Override public String getNome() { return "Ospite"; }
    @Override public String getCognome() { return ""; }
    @Override public String toString() { return "UtenteGenerico{accesso limitato - non autenticato}"; }
}
