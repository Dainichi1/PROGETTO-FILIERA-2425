package unicam.progetto_filiera_springboot.application.service;

import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;

import java.util.List;
import java.util.Optional;

public interface ProdottoService {

    ProdottoResponse creaProdottoConFile(ProdottoForm req,
                                         String usernameCreatore,
                                         List<MultipartFile> foto,
                                         List<MultipartFile> certificati);

    ProdottoResponse uploadFoto(Long prodottoId, List<MultipartFile> files);

    ProdottoResponse uploadCertificati(Long prodottoId, List<MultipartFile> files);

    List<ProdottoResponse> prodottiDi(String usernameCreatore);

    List<ProdottoResponse> listInAttesa();

    void approve(Long id);

    void reject(Long id, Optional<String> commento);

    List<ProdottoResponse> listApprovati();

}
