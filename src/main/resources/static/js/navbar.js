// navbar.js - Universelle Navbar für RadBrain
(function() {
    'use strict';

    // 1. Grundgerüst der Navbar (ohne die wechselnden Buttons rechts)
    const navbarHTML = `
        <nav class="navbar">
            <div class="navbar-content">
                <div class="nav-left">
                    <a href="/" class="nav-logo-link">
                        <img src="/Bilder/logo.png" alt="RadBrain" class="nav-logo">
                    </a>
                    <ul class="nav-menu" id="navMenu">
                        <li class="nav-item">
                            <a href="/userpages/user-home.html" class="nav-link">Home</a>
                        </li>
                        <li class="nav-item dropdown" id="learningAreasDropdown">
                            <a href="#" class="nav-link">
                                Lernbereiche 
<!--                                <span class="dropdown-arrow"><strong>▾</strong></span>-->
                            </a>
                            <ul class="dropdown-menu" id="learningAreasMenu">
                                </ul>
                        </li>
                        <li class="nav-item">
                            <a href="/lernbereiche/table-of-contents.html" class="nav-link">Inhaltsverzeichnis</a>
                        </li>
                        <li class="nav-item">
                            <a href="/quiz/quizzes.html" class="nav-link">Quizzes</a>
                        </li>
                    </ul>
                </div>
                <div class="nav-right" id="navRight">
                    </div>
            </div>
        </nav>
        
        <button class="mobile-menu-toggle" id="mobileMenuToggle" aria-label="Menu">
            <span></span>
            <span></span>
            <span></span>
        </button>
    `;

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
        // Navbar einfügen
        const navPlaceholder = document.getElementById('navbar-placeholder');
        if (navPlaceholder) {
            navPlaceholder.innerHTML = navbarHTML;
        } else if (!document.querySelector('.navbar')) {
            document.body.insertAdjacentHTML('afterbegin', navbarHTML);
        }

        // Mobile Overlay einfügen
        if (!document.getElementById('mobileMenuOverlay')) {
            document.body.insertAdjacentHTML('beforeend', mobileMenuHTML);
        }

        // Reihenfolge: Erst Navigation befüllen, dann Event-Listener und Daten
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
            // GAST-MODUS
            navRight.innerHTML = `
                <div class="auth-buttons">
                    <a href="/auth/login.html" class="btn-login">Anmelden</a>
                    <a href="/auth/register.html" class="btn btn-primary">Registrieren</a>
                </div>
            `;
        } else {
            // USER-MODUS
            const user = window.auth.getUser();
            const displayName = user.fullname ? user.fullname.split(' ')[0] : 'User';
            const isAdmin = window.auth.hasRole('ADMIN');

            navRight.innerHTML = `
                <div class="nav-profile-dropdown">
                    <button class="nav-profile-btn" id="profileBtn">
                        <span class="nav-profile-avatar">
                            ${displayName.charAt(0).toUpperCase()}
                        </span>
                        <span class="nav-profile-name">${displayName}</span>
<!--                        <span class="nav-profile-arrow"><strong>▾</strong></span>-->
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
                        <button onclick="window.auth.logout('/index.html')" class="nav-profile-item nav-profile-logout">
                            Abmelden
                        </button>
                    </div>
                </div>
            `;
            setupProfileDropdown();
        }
    }

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

            const publishedAreas = areas.filter(a => a.isPublished);
            menu.innerHTML = publishedAreas
                .sort((a, b) => a.displayOrder - b.displayOrder)
                .map(area => `
                    <li>
                        <a href="/lernbereiche/learning-area.html?area=${area.slug}" class="dropdown-item">
                            ${area.name}
                        </a>
                    </li>
                `).join('');
        } catch (e) { console.error('Lernbereiche Fehler:', e); }
    }

    function setupMobileMenu() {
        const toggle = document.getElementById('mobileMenuToggle');
        const overlay = document.getElementById('mobileMenuOverlay');
        const close = document.getElementById('mobileMenuClose');

        if (!toggle || !overlay || !close) return;

        toggle.addEventListener('click', () => {
            overlay.classList.add('active');
            renderMobileMenu();
        });
        close.addEventListener('click', () => overlay.classList.remove('active'));
    }

    function renderMobileMenu() {
        const content = document.getElementById('mobileMenuContent');
        if (!content) return;

        const isLoggedIn = window.auth?.isLoggedIn();
        const user = isLoggedIn ? window.auth.getUser() : null;
        const isAdmin = user && window.auth.hasRole('ADMIN');

        let html = `
            <div class="mobile-menu-section">
                <a href="/userpages/user-home.html" class="mobile-menu-item">Home</a>
                <a href="/quiz/quizzes.html" class="mobile-menu-item">Quizzes</a>
            </div>
        `;

        if (isLoggedIn) {
            html += `
                <div class="mobile-menu-divider"></div>
                <div class="mobile-user-info" style="padding: 15px;">
                    <strong>${user.fullname || 'User'}</strong><br><small>${user.email}</small>
                </div>
                <a href="/userpages/account.html" class="mobile-menu-item">Mein Account</a>
                ${isAdmin ? `<a href="/admin/admin-dashboard.html" class="mobile-menu-item">Admin</a>` : ''}
                <button onclick="window.auth.logout('/index.html')" class="mobile-menu-item">Abmelden</button>
            `;
        } else {
            html += `
                <div class="mobile-menu-divider"></div>
                <a href="/auth/login.html" class="mobile-menu-item">Anmelden</a>
                <a href="/auth/register.html" class="mobile-menu-item">Registrieren</a>
            `;
        }
        content.innerHTML = html;
    }

    function setupAuthListener() {
        window.addEventListener('authStateChanged', updateNavigation);
        window.addEventListener('storage', (e) => {
            if (e.key === 'authToken') updateNavigation();
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();