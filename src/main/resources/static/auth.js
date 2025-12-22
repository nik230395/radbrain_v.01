// auth.js - einfacher Auth Helper für frontend
// - Speichert token + userinfo in localStorage
// - Bietet authFetch(url, opts) das Authorization Header anhängt
// - Bietet isLoggedIn(), getUser(), logout(), saveLogin(responseBody)
window.auth = (function(){
    const TOKEN_KEY = 'authToken';
    const EMAIL_KEY = 'userEmail';
    const NAME_KEY = 'userFullname';
    const ROLE_KEY = 'userRole'; // Single role string

    function getToken() { return localStorage.getItem(TOKEN_KEY); }
    function isLoggedIn() { return !!getToken(); }

    // save login response (expects body.token, body.email, body.fullname, body.role)
    function saveLogin(body) {
        if (!body) return;
        if (body.token) localStorage.setItem(TOKEN_KEY, body.token);
        if (body.email) localStorage.setItem(EMAIL_KEY, body.email);
        if (body.fullname) localStorage.setItem(NAME_KEY, body.fullname);
        if (body.role) {
            localStorage.setItem(ROLE_KEY, body.role);
        }
    }

    function logout(redirect) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(EMAIL_KEY);
        localStorage.removeItem(NAME_KEY);
        localStorage.removeItem(ROLE_KEY);
        if (redirect) window.location.href = redirect;
    }

    function getRole() {
        return localStorage.getItem(ROLE_KEY) || '';
    }

    // For backward compatibility, return role as array
    function getRoles() {
        const role = getRole();
        return role ? [role] : [];
    }

    function getUser() {
        return {
            email: localStorage.getItem(EMAIL_KEY),
            fullname: localStorage.getItem(NAME_KEY),
            role: getRole(),
            roles: getRoles() // for backward compatibility
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
        const userRole = getRole();
        return userRole === role || userRole === ('ROLE_' + role) || ('ROLE_' + userRole) === role;
    }

    return {
        isLoggedIn,
        getToken,
        saveLogin,
        logout,
        getUser,
        getRole,
        getRoles,
        hasRole,
        authFetch
    };
})();