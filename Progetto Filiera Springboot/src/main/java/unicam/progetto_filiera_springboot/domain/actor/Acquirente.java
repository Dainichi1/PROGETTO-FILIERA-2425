package unicam.progetto_filiera_springboot.domain.actor;

import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public class Acquirente extends UtenteAutenticato {
    private double fondi = 0.0;
    public Acquirente(String username, String password, String nome, String cognome, double fondi) {
        super(username, password, nome, cognome, Ruolo.ACQUIRENTE);
        this.fondi = fondi;
    }
    public double getFondi() { return fondi; }
    public void setFondi(double fondi) { this.fondi = fondi; }
}
