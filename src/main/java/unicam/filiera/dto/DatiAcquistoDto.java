package unicam.filiera.dto;

import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;
import java.time.LocalDateTime;
import java.util.List;

public class DatiAcquistoDto {
    private String usernameAcquirente;
    private List<CartItemDto> items;         // Gli item acquistati (nome, tipo, quantità, prezzo)
    private double totaleAcquisto;
    private TipoMetodoPagamento tipoMetodoPagamento;
    private StatoPagamento statoPagamento;
    private LocalDateTime timestamp;
    private double fondiPreAcquisto;         // Saldo prima
    private double fondiPostAcquisto;        // Saldo dopo

    public DatiAcquistoDto(String usernameAcquirente, List<CartItemDto> items, double totaleAcquisto,
                           TipoMetodoPagamento tipoMetodoPagamento, StatoPagamento statoPagamento,
                           double fondiPreAcquisto, double fondiPostAcquisto) {
        this.usernameAcquirente = usernameAcquirente;
        this.items = items;
        this.totaleAcquisto = totaleAcquisto;
        this.tipoMetodoPagamento = tipoMetodoPagamento;
        this.statoPagamento = statoPagamento;
        this.timestamp = LocalDateTime.now();
        this.fondiPreAcquisto = fondiPreAcquisto;
        this.fondiPostAcquisto = fondiPostAcquisto;
    }

    // Getter & Setter...

    public String getUsernameAcquirente() { return usernameAcquirente; }
    public List<CartItemDto> getItems() { return items; }
    public double getTotaleAcquisto() { return totaleAcquisto; }
    public TipoMetodoPagamento getTipoMetodoPagamento() { return tipoMetodoPagamento; }
    public StatoPagamento getStatoPagamento() { return statoPagamento; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getFondiPreAcquisto() { return fondiPreAcquisto; }
    public double getFondiPostAcquisto() { return fondiPostAcquisto; }

    public void setStatoPagamento(StatoPagamento statoPagamento) { this.statoPagamento = statoPagamento; }
}
