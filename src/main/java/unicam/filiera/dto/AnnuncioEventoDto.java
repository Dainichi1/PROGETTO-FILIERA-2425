package unicam.filiera.dto;

public class AnnuncioEventoDto {
    private long eventoId;
    private String tipoEvento; // "FIERA" | "VISITA"
    private String titolo;
    private String testo;

    public long getEventoId() { return eventoId; }
    public void setEventoId(long eventoId) { this.eventoId = eventoId; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }
}
