package unicam.filiera.repository;

/** Info per l’indirizzo da mostrare sulla mappa */
public record AddressInfo(String address, String sourceType, long idOrigine) {
    @Override
    public String toString() {
        return address + " (" + sourceType + ")";
    }
}
