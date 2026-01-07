// navbar.js - Universelle Navbar für RadBrain
(function() {
    'use strict';

    function getNavbarHTML() {
        const isLoggedIn = window.auth?.isLoggedIn();

        // DYNAMISCHE LOGIK FÜR HOME/STARTSEITE
        const homeHref = isLoggedIn ? "/userpages/user-home.html" : "/index.html";
        const homeLabel = isLoggedIn ? "Home" : "Startseite";

        return `
        <nav class="navbar">
            <div class="navbar-content">
                <div class="nav-left">
                    <a href="${homeHref}" class="nav-logo-link">
                        <img src="/Bilder/logo.png" alt="RadBrain" class="nav-logo">
                    </a>
                    <ul class="nav-menu" id="navMenu">
                        <li class="nav-item">
                            <a href="${homeHref}" class="nav-link">${homeLabel}</a>
                        </li>
                        <li class="nav-item dropdown" id="learningAreasDropdown">
                            <a href="#" class="nav-link">Lernbereiche</a>
                            <ul class="dropdown-menu" id="learningAreasMenu"></ul>
                        </li>
                        <li class="nav-item">
                            <a href="/lernbereiche/table-of-contents.html" class="nav-link">Inhaltsverzeichnis</a>
                        </li>
                        <li class="nav-item">
                            <a href="/quiz/quizzes.html" class="nav-link">Quizzes</a>
                        </li>
                    </ul>
                </div>
                <div class="nav-right" id="navRight"></div>
            </div>
        </nav>
        
        <button class="mobile-menu-toggle" id="mobileMenuToggle" aria-label="Menu">
            <span></span><span></span><span></span>
        </button>
        `;
    }

    const mobileMenuHTML = `
        <div class="mobile-menu-overlay" id="mobileMenuOverlay">
            <div class="mobile-menu-header">
                <img src="/Bilder/logo.png" alt="RadBrain" class="mobile-logo">
                <button class="mobile-menu-close" id="mobileMenuClose" aria-label="Close">
                    <span>&times;</span>
                </button>
            </div>
            <div class="mobile-menu-content" id="mobileMenuContent"></div>
        </div>
    `;

    function init() {
        const navPlaceholder = document.getElementById('navbar-placeholder');
        const html = getNavbarHTML(); // Generiere HTML basierend auf Auth-Status

        if (navPlaceholder) {
            navPlaceholder.innerHTML = html;
        } else if (!document.querySelector('.navbar')) {
            document.body.insertAdjacentHTML('afterbegin', html);
        }

        if (!document.getElementById('mobileMenuOverlay')) {
            document.body.insertAdjacentHTML('beforeend', mobileMenuHTML);
        }

        updateNavigation();
        setupMobileMenu();
        setupDropdowns();
        loadLearningAreas();
        setupAuthListener();
    }

    function updateNavigation() {
        const navRight = document.getElementById('navRight');
        if (!navRight) return;

        const isLoggedIn = window.auth?.isLoggedIn();

        if (!isLoggedIn) {
            navRight.innerHTML = `
                <div class="auth-buttons">
                    <a href="/auth/login.html" class="btn-login">Anmelden</a>
                    <a href="/auth/register.html" class="btn btn-primary">Registrieren</a>
                </div>
            `;
        } else {
            const user = window.auth.getUser();
            const displayName = user.fullname ? user.fullname.split(' ')[0] : 'User';
            const isAdmin = window.auth.hasRole('ADMIN');

            navRight.innerHTML = `
                <div class="nav-profile-dropdown">
                    <button class="nav-profile-btn" id="profileBtn">
                        <span class="nav-profile-avatar">${displayName.charAt(0).toUpperCase()}</span>
                        <span class="nav-profile-name">${displayName}</span>
                    </button>
                    <div class="nav-profile-menu" id="profileMenu">
                        <div class="nav-profile-header">
                            <div class="nav-profile-info">
                                <div class="nav-profile-fullname">${user.fullname || 'Benutzer'}</div>
                                <div class="nav-profile-email">${user.email || ''}</div>
                            </div>
                        </div>
                        <div class="nav-profile-divider"></div>
                        <a href="/userpages/account.html" class="nav-profile-item">Mein Account</a>
                        ${isAdmin ? `
                            <div class="nav-profile-divider"></div>
                            <a href="/admin/admin-dashboard.html" class="nav-profile-item nav-profile-admin">Admin Dashboard</a>
                        ` : ''}
                        <div class="nav-profile-divider"></div>
                        <button onclick="window.auth.logout('/index.html')" class="nav-profile-item nav-profile-logout">Abmelden</button>
                    </div>
                </div>
            `;
            setupProfileDropdown();
        }
    }

    // Hilfsfunktionen (Dropdowns, Mobile Menu, Auth Listener...)
    function setupProfileDropdown() {
        const profileBtn = document.getElementById('profileBtn');
        const profileMenu = document.getElementById('profileMenu');
        if (!profileBtn || !profileMenu) return;

        profileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const isVisible = profileMenu.style.display === 'block';
            closeAllMenus();
            profileMenu.style.display = isVisible ? 'none' : 'block';
        });
    }

    function setupDropdowns() {
        const dropdowns = document.querySelectorAll('.nav-item.dropdown');
        dropdowns.forEach(dropdown => {
            const menu = dropdown.querySelector('.dropdown-menu');
            dropdown.addEventListener('mouseenter', () => menu.style.display = 'block');
            dropdown.addEventListener('mouseleave', () => menu.style.display = 'none');
        });
        document.addEventListener('click', () => closeAllMenus());
    }

    function closeAllMenus() {
        document.querySelectorAll('.dropdown-menu, .nav-profile-menu').forEach(m => m.style.display = 'none');
    }

    async function loadLearningAreas() {
        try {
            const response = await fetch('http://localhost:8080/api/learning-areas');
            if (!response.ok) return;
            const areas = await response.json();
            const menu = document.getElementById('learningAreasMenu');
            if (!menu) return;

            menu.innerHTML = areas
                .filter(a => a.isPublished)
                .sort((a, b) => a.displayOrder - b.displayOrder)
                .map(area => `<li><a href="/lernbereiche/learning-area.html?area=${area.slug}" class="dropdown-item">${area.name}</a></li>`)
                .join('');
        } catch (e) { console.error('Lernbereiche Fehler:', e); }
    }

    function setupMobileMenu() {
        const toggle = document.getElementById('mobileMenuToggle');
        const overlay = document.getElementById('mobileMenuOverlay');
        const close = document.getElementById('mobileMenuClose');
        if (!toggle || !overlay || !close) return;
        toggle.addEventListener('click', () => { overlay.classList.add('active'); renderMobileMenu(); });
        close.addEventListener('click', () => overlay.classList.remove('active'));
    }

    function renderMobileMenu() {
        const content = document.getElementById('mobileMenuContent');
        if (!content) return;
        const isLoggedIn = window.auth?.isLoggedIn();
        const homeHref = isLoggedIn ? "/userpages/user-home.html" : "/index.html";
        const homeLabel = isLoggedIn ? "Home" : "Startseite";

        let html = `
            <div class="mobile-menu-section">
                <a href="${homeHref}" class="mobile-menu-item">${homeLabel}</a>
                <a href="/quiz/quizzes.html" class="mobile-menu-item">Quizzes</a>
            </div>
        `;
        // ... (Rest deiner Mobile Menu Logik)
        content.innerHTML = html;
    }

    function setupAuthListener() {
        window.addEventListener('authStateChanged', () => {
            // Bei Statusänderung Navbar komplett neu zeichnen
            init();
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();