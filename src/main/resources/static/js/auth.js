// Enhanced auth.js with improved role handling
window.auth = (function() {
    const TOKEN_KEY = 'authToken';
    const EMAIL_KEY = 'userEmail';
    const NAME_KEY = 'userFullname';
    const ROLES_KEY = 'userRoles';
    const USER_ID_KEY = 'userId';

    function getToken() {
        const token = localStorage.getItem(TOKEN_KEY);
        console.log('🔑 Getting token:', token ? 'exists' : 'null');
        return token;
    }

    function isLoggedIn() {
        const loggedIn = !!getToken();
        console.log('✅ isLoggedIn:', loggedIn);
        return loggedIn;
    }

    function saveLogin(body) {
        console.log('💾 saveLogin called with:', body);

        if (!body || !body.token) {
            console.error('❌ No token in response');
            return false;
        }

        localStorage.setItem(TOKEN_KEY, body.token);
        if (body.email) localStorage.setItem(EMAIL_KEY, body.email);
        if (body.fullname) localStorage.setItem(NAME_KEY, body.fullname);
        if (body.id) localStorage.setItem(USER_ID_KEY, body.id);

        // ✅ CRITICAL FIX: Handle role string properly
        let roles = body.roles || 'USER';

        // If roles is a string, convert to array
        if (typeof roles === 'string') {
            // Remove "ROLE_" prefix if present
            const cleanRole = roles.replace('ROLE_', '');
            roles = [cleanRole];
        }

        localStorage.setItem(ROLES_KEY, JSON.stringify(roles));

        console.log('✅ Login data saved successfully. Roles:', roles);

        triggerStateUpdate();

        return true;
    }

    function logout(redirectUrl = '/') {
        console.log('🚪 Logout called');
        localStorage.clear();

        triggerStateUpdate();

        if (redirectUrl) {
            setTimeout(() => {
                window.location.href = redirectUrl;
            }, 100);
        }
    }

    function getUser() {
        const user = {
            id: localStorage.getItem(USER_ID_KEY),
            email: localStorage.getItem(EMAIL_KEY),
            fullname: localStorage.getItem(NAME_KEY),
            roles: getRoles(),
            role: getRoles()[0] // Add single role for compatibility
        };
        return user;
    }

    function getRoles() {
        try {
            const roles = localStorage.getItem(ROLES_KEY);
            return roles ? JSON.parse(roles) : ['USER'];
        } catch (e) {
            console.error('❌ Error parsing roles:', e);
            return ['USER'];
        }
    }

    function hasRole(role) {
        const roles = getRoles();

        // Normalize role for comparison
        const normalizedRole = role.toUpperCase().replace('ROLE_', '');

        const hasIt = roles.some(r => {
            const normalizedR = r.toUpperCase().replace('ROLE_', '');
            return normalizedR === normalizedRole;
        });

        console.log('🔍 Checking role:', role, '| User roles:', roles, '| Has role:', hasIt);

        return hasIt;
    }

    async function authFetch(url, options = {}) {
        options.headers = options.headers || {};
        const token = getToken();

        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }

        console.log('📡 Making authenticated request to:', url);

        const response = await fetch(url, options);

        if (response.status === 401 && token) {
            console.log('🔒 Token expired (401), logging out...');
            logout('/login.html');
            return response;
        }

        if (response.status === 403) {
            console.error('❌ Access forbidden (403) for:', url);
            console.error('User roles:', getRoles());
        }

        return response;
    }

    function triggerStateUpdate() {
        console.log('🔄 Triggering state update across app');

        if (window.updateNavigation) {
            setTimeout(() => window.updateNavigation(), 50);
        }

        window.dispatchEvent(new CustomEvent('authStateChanged', {
            detail: {
                isLoggedIn: isLoggedIn(),
                user: getUser()
            }
        }));

        window.dispatchEvent(new StorageEvent('storage', {
            key: TOKEN_KEY,
            newValue: getToken()
        }));
    }

    function navigateToHome() {
        if (isLoggedIn()) {
            console.log('🏠 Navigating logged user to dashboard');
            window.location.href = '/dashboard.html';
        } else {
            console.log('🏠 Navigating guest to index.html');
            window.location.href = '/';
        }
    }

    function handleLoginSuccess() {
        console.log('✅ Login successful, redirecting to dashboard');
        setTimeout(() => {
            window.location.href = '/dashboard.html';
        }, 1000);
    }

    window.addEventListener('authStateChanged', (event) => {
        console.log('🎭 Auth state changed:', event.detail);
    });

    return {
        isLoggedIn,
        getToken,
        saveLogin,
        logout,
        getUser,
        getCurrentUser: getUser, // Alias for compatibility
        getRoles,
        hasRole,
        authFetch,
        triggerStateUpdate,
        navigateToHome,
        handleLoginSuccess
    };
})();

console.log('🚀 Enhanced auth.js loaded, current state:', {
    loggedIn: window.auth.isLoggedIn(),
    user: window.auth.isLoggedIn() ? window.auth.getUser() : null
});