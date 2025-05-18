package unicam.filiera.service;

import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.model.TipoMetodoPagamento;

public class PagamentoServiceImpl implements PagamentoService {

    @Override
    public StatoPagamento effettuaPagamento(DatiAcquistoDto dati) {
        TipoMetodoPagamento metodo = dati.getTipoMetodoPagamento();

        if (metodo == null) return StatoPagamento.RIFIUTATO;

        // Se è PAGAMENTO ALLA CONSEGNA, sempre accettato
        if (metodo == TipoMetodoPagamento.PAGAMENTO_ALLA_CONSEGNA) {
            return StatoPagamento.APPROVATO;
        }

        // Altri metodi: simulazione esito (es. carta, paypal ecc.)
        double random = Math.random(); // valore tra 0.0 e 1.0
        if (random < 0.8) {
            return StatoPagamento.APPROVATO; // 80% successo
        } else {
            return StatoPagamento.RIFIUTATO; // 20% fallito
        }
    }
}
