// Enhanced auth.js with state management and smart routing
window.auth = (function() {
    const TOKEN_KEY = 'authToken';
    const EMAIL_KEY = 'userEmail';
    const NAME_KEY = 'userFullname';
    const ROLES_KEY = 'userRoles';
    const USER_ID_KEY = 'userId';

    function getToken() {
        const token = localStorage.getItem(TOKEN_KEY);
        console.log('🔑 Getting token:', token ?  'exists' : 'null');
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
        if (body.fullname) localStorage.setItem(NAME_KEY, body. fullname);
        if (body.id) localStorage.setItem(USER_ID_KEY, body.id);

        let roles = body.roles || [];
        if (typeof roles === 'string') roles = roles.split(',');
        localStorage.setItem(ROLES_KEY, JSON.stringify(roles));

        console.log('✅ Login data saved successfully');

        // Trigger state updates across all pages
        triggerStateUpdate();

        return true;
    }

    function logout(redirectUrl = '/') {
        console.log('🚪 Logout called');
        localStorage.clear();

        // Trigger state updates
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
            fullname:  localStorage.getItem(NAME_KEY),
            roles: getRoles()
        };
        return user;
    }

    function getRoles() {
        try {
            const roles = localStorage. getItem(ROLES_KEY);
            return roles ? JSON.parse(roles) : [];
        } catch (e) {
            return [];
        }
    }

    function hasRole(role) {
        const roles = getRoles();
        const hasIt = roles. some(r =>
            r === role ||
            r === 'ROLE_' + role ||
            r. toUpperCase() === role.toUpperCase()
        );
        return hasIt;
    }

    async function authFetch(url, options = {}) {
        options.headers = options.headers || {};
        const token = getToken();

        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }

        const response = await fetch(url, options);

        if (response.status === 401 && token) {
            console.log('🔒 Token expired, logging out.. .');
            logout('/login.html');
            return response;
        }

        return response;
    }

    // Enhanced state management
    function triggerStateUpdate() {
        console.log('🔄 Triggering state update across app');

        // Update navigation if function exists
        if (window. updateNavigation) {
            setTimeout(() => window.updateNavigation(), 50);
        }

        // Dispatch custom event for other components
        window.dispatchEvent(new CustomEvent('authStateChanged', {
            detail: {
                isLoggedIn: isLoggedIn(),
                user: getUser()
            }
        }));

        // Also trigger storage event for other tabs
        window.dispatchEvent(new StorageEvent('storage', {
            key: TOKEN_KEY,
            newValue: getToken()
        }));
    }

    // Smart navigation function
    function navigateToHome() {
        if (isLoggedIn()) {
            console.log('🏠 Navigating logged user to user-home.html');
            window.location.href = '/user-home. html';
        } else {
            console.log('🏠 Navigating guest to index. html');
            window.location. href = '/';
        }
    }

    // Enhanced login success handler
    function handleLoginSuccess() {
        console.log('✅ Login successful, redirecting to user home');
        setTimeout(() => {
            window.location.href = '/user-home.html';
        }, 1000);
    }

    // Listen for auth state changes
    window. addEventListener('authStateChanged', (event) => {
        console.log('🎭 Auth state changed:', event.detail);
    });

    // ✅ Single return statement with ALL methods
    return {
        isLoggedIn,
        getToken,
        saveLogin,
        logout,
        getUser,
        getRoles,
        hasRole,
        authFetch,
        triggerStateUpdate,
        navigateToHome,
        handleLoginSuccess
    };
})();

// Debug info
console.log('🚀 Enhanced auth.js loaded, current state:', {
    loggedIn: window.auth. isLoggedIn(),
    user: window.auth.isLoggedIn() ? window.auth.getUser() : null
});