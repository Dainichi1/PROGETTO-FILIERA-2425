package unicam.filiera.model;

import java.time.LocalDateTime;

public class PrenotazioneFiera {
    private long id;
    private long idFiera;
    private String usernameAcquirente;
    private int numeroPersone;
    private LocalDateTime dataPrenotazione;

    // Costruttore vuoto (obbligatorio per JDBC/Bean)
    public PrenotazioneFiera() {
    }

    // Costruttore completo
    public PrenotazioneFiera(long id, long idFiera, String usernameAcquirente, int numeroPersone, LocalDateTime dataPrenotazione) {
        this.id = id;
        this.idFiera = idFiera;
        this.usernameAcquirente = usernameAcquirente;
        this.numeroPersone = numeroPersone;
        this.dataPrenotazione = dataPrenotazione;
    }

    // Costruttore senza id (per inserimenti)
    public PrenotazioneFiera(long idFiera, String usernameAcquirente, int numeroPersone, LocalDateTime dataPrenotazione) {
        this.idFiera = idFiera;
        this.usernameAcquirente = usernameAcquirente;
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

    public long getIdFiera() {
        return idFiera;
    }

    public void setIdFiera(long idFiera) {
        this.idFiera = idFiera;
    }

    public String getUsernameAcquirente() {
        return usernameAcquirente;
    }

    public void setUsernameAcquirente(String usernameAcquirente) {
        this.usernameAcquirente = usernameAcquirente;
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
        return "PrenotazioneFiera{" +
                "id=" + id +
                ", idFiera=" + idFiera +
                ", usernameAcquirente='" + usernameAcquirente + '\'' +
                ", numeroPersone=" + numeroPersone +
                ", dataPrenotazione=" + dataPrenotazione +
                '}';
    }
}
