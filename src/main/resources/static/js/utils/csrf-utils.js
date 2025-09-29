/**
 * Recupera token CSRF e nome header da <meta>.
 */
export const csrfUtils = {
    getCsrf: () => {
        const tokenEl = document.querySelector('meta[name="_csrf"]');
        const headerEl = document.querySelector('meta[name="_csrf_header"]');
        return {
            token: tokenEl ? tokenEl.getAttribute('content') : '',
            header: headerEl ? headerEl.getAttribute('content') : 'X-CSRF-TOKEN'
        };
    }
};
