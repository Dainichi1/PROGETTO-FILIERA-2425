package unicam.filiera.dto;

public class AcquistoItemDto {
    private final String nomeItem;
    private final String tipoItem;
    private final int quantita;
    private final double prezzoUnitario;
    private final double totale;

    public AcquistoItemDto(String nomeItem, String tipoItem, int quantita,
                           double prezzoUnitario, double totale) {
        this.nomeItem = nomeItem;
        this.tipoItem = tipoItem;
        this.quantita = quantita;
        this.prezzoUnitario = prezzoUnitario;
        this.totale = totale;
    }

    public String getNomeItem() { return nomeItem; }
    public String getTipoItem() { return tipoItem; }
    public int getQuantita() { return quantita; }
    public double getPrezzoUnitario() { return prezzoUnitario; }
    public double getTotale() { return totale; }
}
