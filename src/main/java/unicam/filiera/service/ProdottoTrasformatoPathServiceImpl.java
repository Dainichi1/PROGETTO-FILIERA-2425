package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import unicam.filiera.dto.FaseProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoPathDto;
import unicam.filiera.entity.FaseProduzioneEmbeddable;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.repository.ProdottoTrasformatoRepository;
import unicam.filiera.repository.ProdottoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProdottoTrasformatoPathServiceImpl implements ProdottoTrasformatoPathService {

    private final ProdottoTrasformatoRepository trasformatoRepo;
    private final ProdottoRepository prodottoRepo;
    private final IndirizzoService indirizzoService; // per riusare geocoding già esistente

    @Autowired
    public ProdottoTrasformatoPathServiceImpl(
            ProdottoTrasformatoRepository trasformatoRepo,
            ProdottoRepository prodottoRepo,
            IndirizzoService indirizzoService) {
        this.trasformatoRepo = trasformatoRepo;
        this.prodottoRepo = prodottoRepo;
        this.indirizzoService = indirizzoService;
    }

    @Override
    public ProdottoTrasformatoPathDto getPath(Long trasformatoId) {
        ProdottoTrasformatoEntity trasformato = trasformatoRepo.findById(trasformatoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prodotto trasformato non trovato: " + trasformatoId));

        // Geocode indirizzo del prodotto trasformato
        double[] coordsTrasformato = geocode(trasformato.getIndirizzo());
        if (coordsTrasformato == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indirizzo non valido per prodotto trasformato: " + trasformato.getNome()
            );
        }

        // Recupero fasi e calcolo coordinate prodotti origine
        List<FaseProdottoDto> fasiDto = new ArrayList<>();
        for (FaseProduzioneEmbeddable f : trasformato.getFasiProduzione()) {
            ProdottoEntity prodotto = prodottoRepo.findById(f.getProdottoOrigineId()).orElse(null);
            if (prodotto != null) {
                double[] coordsProdotto = geocode(prodotto.getIndirizzo());
                if (coordsProdotto != null) {
                    fasiDto.add(new FaseProdottoDto(
                            prodotto.getId(),
                            prodotto.getNome(),
                            coordsProdotto[0],
                            coordsProdotto[1],
                            f.getDescrizioneFase()
                    ));
                } else {
                    System.err.println("⚠️ Geocoding fallito per prodotto origine: " + prodotto.getNome());
                }
            } else {
                System.err.println("⚠️ Prodotto origine mancante per fase: " + f.getDescrizioneFase());
            }
        }

        return new ProdottoTrasformatoPathDto(
                trasformato.getId(),
                trasformato.getNome(),
                coordsTrasformato[0],
                coordsTrasformato[1],
                fasiDto
        );
    }

    private double[] geocode(String indirizzo) {
        var dto = indirizzoService.geocodeIndirizzo(
                new unicam.filiera.dto.IndirizzoDto(null, indirizzo, "Generico", "Geocode")
        );
        if (dto != null && dto.getLat() != null && dto.getLng() != null) {
            return new double[]{dto.getLat(), dto.getLng()};
        }
        return null; // nessun fallback → il frontend gestisce l'errore
    }
}
