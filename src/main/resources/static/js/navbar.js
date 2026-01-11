// navbar.js - Optimierte universelle Navbar für RadBrain
(function() {
    'use strict';

    /**
     * Erzeugt das Grundgerüst der Navbar basierend auf dem Auth-Status.
     */
    function getNavbarHTML() {
        const isLoggedIn = window.auth?.isLoggedIn();

        // Dynamische Ziele und Labels für Home/Startseite
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
                            <a href="javascript:void(0)" class="nav-link">Lernmodule<small>▾</small></a>
                            <ul class="dropdown-menu" id="learningAreasMenu">
                                <li><span class="dropdown-item" style="color: #999;">Lade Bereiche...</span></li>
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a href="/lernbereiche/table-of-contents.html" class="nav-link">Inhalt</a>
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
            <span></span><span></span><span></span>
        </button>

        <div class="mobile-menu-overlay" id="mobileMenuOverlay">
            <div class="mobile-menu-header">
                <img src="/Bilder/logo.png" alt="RadBrain" class="mobile-logo">
                <button class="mobile-menu-close" id="mobileMenuClose">&times;</button>
            </div>
            <div class="mobile-menu-content" id="mobileMenuContent"></div>
        </div>
        `;
    }

    /**
     * Initialisiert die Navbar und alle Event-Listener.
     */
    function init() {
        const navPlaceholder = document.getElementById('navbar-placeholder');
        const html = getNavbarHTML();

        if (navPlaceholder) {
            navPlaceholder.innerHTML = html;
        } else if (!document.querySelector('.navbar')) {
            document.body.insertAdjacentHTML('afterbegin', html);
        }

        updateNavigation();
        setupMobileMenu();
        setupDropdownHoverLogic(); // Verbessertes Dropdown-Verhalten
        loadLearningAreas();
        setupAuthListener();
    }

    /**
     * Steuert die Buttons auf der rechten Seite (Login vs. Profil).
     */
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
                        <small>▾</small>
                    </button>
                    <div class="nav-profile-menu" id="profileMenu">
                        <div class="nav-profile-header">
                            <div class="nav-profile-fullname">${user.fullname || 'Benutzer'}</div>
                            <div class="nav-profile-email">${user.email || ''}</div>
                        </div>
                        <div class="nav-profile-divider"></div>
                        <a href="/userpages/account.html" class="nav-profile-item">Mein Account</a>
                        ${isAdmin ? `
                            <a href="/admin/admin-dashboard.html" class="nav-profile-item admin-link">Admin Dashboard</a>
                        ` : ''}
                        <div class="nav-profile-divider"></div>
                        <button onclick="window.auth.logout('/index.html')" class="nav-profile-item logout-btn">
                            Abmelden
                        </button>
                    </div>
                </div>
            `;
            setupProfileClickLogic();
        }
    }

    /**
     * Verhindert das "Weglaufen" des Dropdowns durch einen kleinen Puffer.
     */
    function setupDropdownHoverLogic() {
        const dropdowns = document.querySelectorAll('.nav-item.dropdown');
        dropdowns.forEach(dropdown => {
            const menu = dropdown.querySelector('.dropdown-menu');
            let timeout;

            dropdown.addEventListener('mouseenter', () => {
                clearTimeout(timeout);
                closeAllMenus(); // Schließt andere offene Menüs
                menu.style.display = 'block';
            });

            dropdown.addEventListener('mouseleave', () => {
                // 200ms Puffer: Menü bleibt kurz offen, falls man mit der Maus verrutscht
                timeout = setTimeout(() => {
                    menu.style.display = 'none';
                }, 200);
            });
        });
    }

    /**
     * Profil-Menü öffnet sich per Klick (stabiler für User-Aktionen).
     */
    function setupProfileClickLogic() {
        const profileBtn = document.getElementById('profileBtn');
        const profileMenu = document.getElementById('profileMenu');
        if (!profileBtn) return;

        profileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const isVisible = profileMenu.style.display === 'block';
            closeAllMenus();
            profileMenu.style.display = isVisible ? 'none' : 'block';
        });

        document.addEventListener('click', () => closeAllMenus());
    }

    function closeAllMenus() {
        document.querySelectorAll('.dropdown-menu, .nav-profile-menu').forEach(m => {
            m.style.display = 'none';
        });
    }

    /**
     * Lädt die Lernbereiche aus dem Backend und befüllt das Menü.
     */
    async function loadLearningAreas() {
        try {
            const response = await fetch('http://localhost:8080/api/learning-areas');
            if (!response.ok) return;
            const areas = await response.json();
            const menu = document.getElementById('learningAreasMenu');
            if (!menu) return;

            const items = areas
                .filter(a => a.isPublished)
                .sort((a, b) => a.displayOrder - b.displayOrder)
                .map(area => `
                    <li>
                        <a href="/lernbereiche/learning-area.html?area=${area.slug}" class="dropdown-item">
                            ${area.name}
                        </a>
                    </li>
                `).join('');

            menu.innerHTML = items || '<li><span class="dropdown-item">Keine Inhalte</span></li>';
        } catch (e) {
            console.error('Navbar-Inhalte konnten nicht geladen werden:', e);
        }
    }

    /**
     * Mobile Menu Logik inkl. vollständiger Link-Liste.
     */
    function setupMobileMenu() {
        const toggle = document.getElementById('mobileMenuToggle');
        const overlay = document.getElementById('mobileMenuOverlay');
        const close = document.getElementById('mobileMenuClose');

        if (!toggle || !overlay || !close) return;

        toggle.addEventListener('click', () => {
            overlay.classList.add('active');
            renderMobileMenuContent();
        });

        close.addEventListener('click', () => overlay.classList.remove('active'));
    }

    function renderMobileMenuContent() {
        const content = document.getElementById('mobileMenuContent');
        if (!content) return;

        const isLoggedIn = window.auth?.isLoggedIn();
        const homeHref = isLoggedIn ? "/userpages/user-home.html" : "/index.html";
        const homeLabel = isLoggedIn ? "Home" : "Startseite";

        content.innerHTML = `
            <div class="mobile-menu-section">
                <a href="${homeHref}" class="mobile-menu-item">${homeLabel}</a>
                <a href="/lernbereiche/table-of-contents.html" class="mobile-menu-item">Inhalt</a>
                <a href="/quiz/quizzes.html" class="mobile-menu-item">Quizzes</a>
            </div>
            <div class="mobile-menu-divider"></div>
            <div class="mobile-menu-section">
                ${isLoggedIn ? `
                    <a href="/userpages/account.html" class="mobile-menu-item">Mein Account</a>
                    <button onclick="window.auth.logout('/index.html')" class="mobile-menu-item logout-text">Abmelden</button>
                ` : `
                    <a href="/auth/login.html" class="mobile-menu-item">Anmelden</a>
                    <a href="/auth/register.html" class="mobile-menu-item">Registrieren</a>
                `}
            </div>
        `;
    }

    function setupAuthListener() {
        window.addEventListener('authStateChanged', () => init());
    }

    // Start der Navbar
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();