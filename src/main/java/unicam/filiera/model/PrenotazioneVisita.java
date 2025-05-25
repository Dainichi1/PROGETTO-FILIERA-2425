package unicam.filiera.model;

import java.time.LocalDateTime;

/**
 * Rappresenta una prenotazione per una visita aziendale/invito.
 * Può essere utilizzata da Produttore, Trasformatore e Distributore.
 */
public class PrenotazioneVisita {
    private long id;
    private long idVisita;
    private String usernameVenditore;
    private int numeroPersone;
    private LocalDateTime dataPrenotazione;

    // Costruttore vuoto (necessario per JDBC/Bean)
    public PrenotazioneVisita() {
    }

    // Costruttore completo
    public PrenotazioneVisita(long id, long idVisita, String usernameVenditore, int numeroPersone, LocalDateTime dataPrenotazione) {
        this.id = id;
        this.idVisita = idVisita;
        this.usernameVenditore = usernameVenditore;
        this.numeroPersone = numeroPersone;
        this.dataPrenotazione = dataPrenotazione;
    }

    // Costruttore senza id (per inserimenti)
    public PrenotazioneVisita(long idVisita, String usernameVenditore, int numeroPersone, LocalDateTime dataPrenotazione) {
        this.idVisita = idVisita;
        this.usernameVenditore = usernameVenditore;
        this.numeroPersone = numeroPersone;
        this.dataPrenotazione = dataPrenotazione;
    }

    // Getter e Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(long idVisita) {
        this.idVisita = idVisita;
    }

    public String getUsernameVenditore() {
        return usernameVenditore;
    }

    public void setUsernameVenditore(String usernameVenditore) {
        this.usernameVenditore = usernameVenditore;
    }

    public int getNumeroPersone() {
        return numeroPersone;
    }

    public void setNumeroPersone(int numeroPersone) {
        this.numeroPersone = numeroPersone;
    }

    public LocalDateTime getDataPrenotazione() {
        return dataPrenotazione;
    }

    public void setDataPrenotazione(LocalDateTime dataPrenotazione) {
        this.dataPrenotazione = dataPrenotazione;
    }

    @Override
    public String toString() {
        return "PrenotazioneVisita{" +
                "id=" + id +
                ", idVisita=" + idVisita +
                ", usernameVenditore='" + usernameVenditore + '\'' +
                ", numeroPersone=" + numeroPersone +
                ", dataPrenotazione=" + dataPrenotazione +
                '}';
    }
}
