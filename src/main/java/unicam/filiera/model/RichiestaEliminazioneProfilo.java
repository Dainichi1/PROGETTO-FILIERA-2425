package unicam.filiera.model;

import java.time.LocalDateTime;

public class RichiestaEliminazioneProfilo {
    private int id; // ID richiesta
    private String username; // Username dell'utente che ha richiesto l'eliminazione
    private StatoRichiestaEliminazioneProfilo stato;
    private LocalDateTime dataRichiesta;

    public RichiestaEliminazioneProfilo(int id, String username, StatoRichiestaEliminazioneProfilo stato, LocalDateTime dataRichiesta) {
        this.id = id;
        this.username = username;
        this.stato = stato;
        this.dataRichiesta = dataRichiesta;
    }

    // Costruttore senza ID (per inserimento)
    public RichiestaEliminazioneProfilo(String username, StatoRichiestaEliminazioneProfilo stato, LocalDateTime dataRichiesta) {
        this(-1, username, stato, dataRichiesta);
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public StatoRichiestaEliminazioneProfilo getStato() { return stato; }
    public void setStato(StatoRichiestaEliminazioneProfilo stato) { this.stato = stato; }

    public LocalDateTime getDataRichiesta() { return dataRichiesta; }
    public void setDataRichiesta(LocalDateTime dataRichiesta) { this.dataRichiesta = dataRichiesta; }
}
