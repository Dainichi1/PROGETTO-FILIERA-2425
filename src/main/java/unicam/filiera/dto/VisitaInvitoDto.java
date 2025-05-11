package unicam.filiera.dto;

import java.util.List;

/**
 * DTO per la creazione di una visita su invito.
 */
public class VisitaInvitoDto {
    private final String dataInizioTxt;
    private final String dataFineTxt;
    private final String prezzoTxt;
    private final String descrizione;
    private final String indirizzo;
    private final String minPartecipantiTxt;
    private final List<String> destinatari;

    public VisitaInvitoDto(
            String dataInizioTxt,
            String dataFineTxt,
            String prezzoTxt,
            String descrizione,
            String indirizzo,
            String minPartecipantiTxt,
            List<String> destinatari
    ) {
        this.dataInizioTxt = dataInizioTxt;
        this.dataFineTxt = dataFineTxt;
        this.prezzoTxt = prezzoTxt;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.minPartecipantiTxt = minPartecipantiTxt;
        this.destinatari = List.copyOf(destinatari);
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

    public List<String> getDestinatari() {
        return destinatari;
    }
}
