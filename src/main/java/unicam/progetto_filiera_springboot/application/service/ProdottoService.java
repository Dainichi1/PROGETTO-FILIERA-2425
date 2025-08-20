package unicam.progetto_filiera_springboot.application.service;

import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;

import java.util.List;

public interface ProdottoService {

    // Crea prodotto (stato IN_ATTESA) + upload foto e certificati obbligatori.
    ProdottoResponse creaProdottoConFile(ProdottoForm form,
                                         String usernameCreatore,
                                         List<MultipartFile> foto,
                                         List<MultipartFile> certificati);

    // Upload opzionali post-creazione
    ProdottoResponse uploadFoto(Long prodottoId, List<MultipartFile> files);

    ProdottoResponse uploadCertificati(Long prodottoId, List<MultipartFile> files);

    List<ProdottoResponse> prodottiDi(String usernameCreatore);
}
