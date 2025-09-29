package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;

@Service
public class PagamentoServiceImpl implements PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoServiceImpl.class);

    // Contatore globale delle transazioni
    private int contatoreTransazioni = 0;

    // Ogni quante transazioni deve fallire (es. ogni 5 pagamenti → 1 fallisce)
    private static final int FREQUENZA_FALLIMENTO = 5;

    @Override
    public StatoPagamento effettuaPagamento(DatiAcquistoDto dati) {
        TipoMetodoPagamento metodo = dati.getTipoMetodoPagamento();

        if (metodo == null) {
            log.warn("Pagamento rifiutato: metodo di pagamento nullo");
            return StatoPagamento.RIFIUTATO;
        }

        // Pagamento alla consegna sempre accettato
        if (metodo == TipoMetodoPagamento.PAGAMENTO_ALLA_CONSEGNA) {
            log.info("Pagamento alla consegna accettato automaticamente per utente [{}]", dati.getUsernameAcquirente());
            return StatoPagamento.APPROVATO;
        }

        contatoreTransazioni++;

        // Ogni N-esima transazione con carta o bonifico viene rifiutata
        if (contatoreTransazioni % FREQUENZA_FALLIMENTO == 0) {
            log.warn("Pagamento RIFIUTATO (simulazione) per utente [{}], metodo [{}] - transazione #{}",
                    dati.getUsernameAcquirente(), metodo, contatoreTransazioni);
            return StatoPagamento.RIFIUTATO;
        }

        // Tutte le altre transazioni vanno a buon fine
        log.info("Pagamento APPROVATO per utente [{}], metodo [{}] - transazione #{}",
                dati.getUsernameAcquirente(), metodo, contatoreTransazioni);
        return StatoPagamento.APPROVATO;
    }
}
