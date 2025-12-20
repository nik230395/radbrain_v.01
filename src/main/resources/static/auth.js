// auth.js - einfacher Auth Helper für frontend
// - Speichert token + userinfo in localStorage
// - Bietet authFetch(url, opts) das Authorization Header anhängt
// - Bietet isLoggedIn(), getUser(), logout(), saveLogin(responseBody)
window.auth = (function(){
    const TOKEN_KEY = 'authToken';
    const EMAIL_KEY = 'userEmail';
    const NAME_KEY = 'userFullname';
    const ROLES_KEY = 'userRoles'; // JSON array string

    function getToken() { return localStorage.getItem(TOKEN_KEY); }
    function isLoggedIn() { return !!getToken(); }

    // save login response (expects body.token, body.email, body.fullname, body.roles)
    function saveLogin(body) {
        if (!body) return;
        if (body.token) localStorage.setItem(TOKEN_KEY, body.token);
        if (body.email) localStorage.setItem(EMAIL_KEY, body.email);
        if (body.fullname) localStorage.setItem(NAME_KEY, body.fullname);
        if (body.roles) {
            const r = Array.isArray(body.roles) ? body.roles : (typeof body.roles === 'string' ? body.roles.split(',') : []);
            localStorage.setItem(ROLES_KEY, JSON.stringify(r));
        }
    }

    function logout(redirect) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(EMAIL_KEY);
        localStorage.removeItem(NAME_KEY);
        localStorage.removeItem(ROLES_KEY);
        if (redirect) window.location.href = redirect;
    }

    function getRoles() {
        try {
            const r = localStorage.getItem(ROLES_KEY);
            return r ? JSON.parse(r) : [];
        } catch (e) { return []; }
    }

    function getUser() {
        return {
            email: localStorage.getItem(EMAIL_KEY),
            fullname: localStorage.getItem(NAME_KEY),
            roles: getRoles()
        };
    }

    async function authFetch(url, opts = {}) {
        opts.headers = opts.headers || {};
        const token = getToken();
        if (token) {
            opts.headers['Authorization'] = 'Bearer ' + token;
        }
        return fetch(url, opts);
    }

    function hasRole(role) {
        return getRoles().some(r => r === role || r === ('ROLE_' + role));
    }

    return {
        isLoggedIn,
        getToken,
        saveLogin,
        logout,
        getUser,
        getRoles,
        hasRole,
        authFetch
    };
})();