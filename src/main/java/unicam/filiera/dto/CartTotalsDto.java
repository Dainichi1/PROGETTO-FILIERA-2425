package unicam.filiera.dto;

public class CartTotalsDto {
    private final int    totaleArticoli;
    private final double costoTotale;

    public CartTotalsDto(int totaleArticoli, double costoTotale) {
        this.totaleArticoli = totaleArticoli;
        this.costoTotale    = costoTotale;
    }

    public int    getTotaleArticoli() { return totaleArticoli; }
    public double getCostoTotale()    { return costoTotale;    }
}
