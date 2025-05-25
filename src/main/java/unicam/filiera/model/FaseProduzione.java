package unicam.filiera.model;

public class FaseProduzione {
    private final String descrizioneFase;
    private final String produttoreUsername;     // Username del produttore
    private final String prodottoOrigine;        // Nome (o id) del prodotto collegato

    // Costruttore per DAO/DTO
    public FaseProduzione(String descrizioneFase, String produttoreUsername, String prodottoOrigine) {
        this.descrizioneFase = descrizioneFase;
        this.produttoreUsername = produttoreUsername;
        this.prodottoOrigine = prodottoOrigine;
    }

    public String getDescrizioneFase() {
        return descrizioneFase;
    }

    public String getProduttoreUsername() {
        return produttoreUsername;
    }

    public String getProdottoOrigine() {
        return prodottoOrigine;
    }

    public static FaseProduzione fromDto(unicam.filiera.dto.ProdottoTrasformatoDto.FaseProduzioneDto dto) {
        return new FaseProduzione(
                dto.getDescrizioneFase(),
                dto.getProduttore(),
                dto.getProdottoProduttore()
        );
    }

    @Override
    public String toString() {
        return "[Fase: " + descrizioneFase +
                ", Produttore: " + produttoreUsername +
                ", Prodotto: " + prodottoOrigine +
                "]";
    }
}
