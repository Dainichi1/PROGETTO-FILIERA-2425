import { formUtilsCrud } from "./crud-form-utils.js";
import { deleteUtilsCrud } from "./crud-delete-utils.js";
import { editUtilsCrud } from "./crud-edit-utils.js";
import { socialUtilsCrud } from "./crud-social-utils.js";
import { prenotazioniUtils } from "./crud-prenotazioni-utils.js";
import { validationUtilsCrud } from "./crud-validation-utils.js";

/**
 * Core CRUD Utils - wrapper che centralizza i vari moduli
 */
export const crudUtils = {
    form: formUtilsCrud,
    delete: deleteUtilsCrud,
    edit: editUtilsCrud,
    social: socialUtilsCrud,
    prenotazioni: prenotazioniUtils,
    validation: validationUtilsCrud,

    // Espongo funzioni chiave direttamente (shortcut)
    openSocialModal: socialUtilsCrud.openSocialModal,
    openDeletePrenotazioneModal: prenotazioniUtils.openDeletePrenotazioneModal
};
