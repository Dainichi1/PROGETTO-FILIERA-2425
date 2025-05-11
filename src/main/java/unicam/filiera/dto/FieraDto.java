// -------- FieraDto.java --------
package unicam.filiera.dto;

public class FieraDto {
    private final String dataInizioTxt;
    private final String dataFineTxt;
    private final String prezzoTxt;
    private final String descrizione;
    private final String indirizzo;
    private final String minPartecipantiTxt;

    public FieraDto(String dataInizioTxt,
                    String dataFineTxt,
                    String prezzoTxt,
                    String descrizione,
                    String indirizzo,
                    String minPartecipantiTxt) {
        this.dataInizioTxt = dataInizioTxt;
        this.dataFineTxt = dataFineTxt;
        this.prezzoTxt = prezzoTxt;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.minPartecipantiTxt = minPartecipantiTxt;
    }

    public String getDataInizioTxt() {
        return dataInizioTxt;
    }

    public String getDataFineTxt() {
        return dataFineTxt;
    }

    public String getPrezzoTxt() {
        return prezzoTxt;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getMinPartecipantiTxt() {
        return minPartecipantiTxt;
    }

}
