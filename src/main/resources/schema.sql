ALTER TABLE prodotti DROP CONSTRAINT IF EXISTS chk_quantita_prodotti;
ALTER TABLE prodotti
    ADD CONSTRAINT chk_quantita_prodotti CHECK (
        CASE WHEN id IS NULL THEN quantita > 0 ELSE quantita >= 0 END
        );

ALTER TABLE pacchetti DROP CONSTRAINT IF EXISTS chk_quantita_pacchetti;
ALTER TABLE pacchetti
    ADD CONSTRAINT chk_quantita_pacchetti CHECK (
        CASE WHEN id IS NULL THEN quantita > 0 ELSE quantita >= 0 END
        );

ALTER TABLE prodotti_trasformati DROP CONSTRAINT IF EXISTS chk_quantita_prodotti_trasformati;
ALTER TABLE prodotti_trasformati
    ADD CONSTRAINT chk_quantita_prodotti_trasformati CHECK (
        CASE WHEN id IS NULL THEN quantita > 0 ELSE quantita >= 0 END
        );


