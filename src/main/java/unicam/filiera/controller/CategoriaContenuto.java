package unicam.filiera.controller;

public enum CategoriaContenuto {
    UTENTI("Utenti registrati"),
    PRODOTTI("Prodotti"),
    PACCHETTI("Pacchetti"),
    PRODOTTI_TRASFORMATI("Prodotti trasformati"),
    FIERE("Fiere / Eventi"),
    VISITE_INVITO("Visite su invito"),
    ACQUISTI("Acquisti / Transazioni"),
    PRENOTAZIONI_FIERE("Prenotazioni fiere"),
    PRENOTAZIONI_VISITE("Prenotazioni visite"),
    SOCIAL_POSTS("Post social");

    private final String label;
    CategoriaContenuto(String label){ this.label = label; }
    public String label(){ return label; }

    @Override public String toString(){ return label; }
}
