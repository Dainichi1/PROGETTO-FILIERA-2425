package unicam.filiera.dto;

import java.time.LocalDateTime;

public class AcquistoListaDto {
    private final int id;
    private final String usernameAcquirente;
    private final double totale;
    private final String statoPagamento;         // stringa per tabella
    private final String tipoMetodoPagamento;    // stringa per tabella
    private final LocalDateTime dataOra;
    private final String elencoItem;

    public AcquistoListaDto(int id, String usernameAcquirente, double totale,
                            String statoPagamento, String tipoMetodoPagamento,
                            LocalDateTime dataOra, String elencoItem) {
        this.id = id;
        this.usernameAcquirente = usernameAcquirente;
        this.totale = totale;
        this.statoPagamento = statoPagamento;
        this.tipoMetodoPagamento = tipoMetodoPagamento;
        this.dataOra = dataOra;
        this.elencoItem = elencoItem;
    }

    public int getId() {
        return id;
    }

    public String getUsernameAcquirente() {
        return usernameAcquirente;
    }

    public double getTotale() {
        return totale;
    }

    public String getStatoPagamento() {
        return statoPagamento;
    }

    public String getTipoMetodoPagamento() {
        return tipoMetodoPagamento;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public String getElencoItem() {
        return elencoItem;
    }
}
