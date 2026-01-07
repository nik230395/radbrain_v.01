// navbar.js - Universelle Navbar für alle Seiten
(function() {
    'use strict';

    // Navbar HTML Template
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
                                Lernbereiche <span class="dropdown-arrow">▾</span>
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
                    <a href="/auth/login.html" class="btn-login">Anmelden</a>
                    <a href="/auth/register.html" class="btn btn-primary">Registrieren</a>
                </div>
            </div>
        </nav>
        
        <!-- Mobile Menu Toggle -->
        <button class="mobile-menu-toggle" id="mobileMenuToggle" aria-label="Menu">
            <span></span>
            <span></span>
            <span></span>
        </button>
    `;

    // Mobile Menu Overlay HTML
    const mobileMenuHTML = `
        <div class="mobile-menu-overlay" id="mobileMenuOverlay">
            <div class="mobile-menu-header">
                <img src="/Bilder/logo.png" alt="RadBrain" class="mobile-logo">
                <button class="mobile-menu-close" id="mobileMenuClose" aria-label="Close">
                    <span>&times;</span>
                </button>
            </div>
            <div class="mobile-menu-content" id="mobileMenuContent">
                <!-- Content wird dynamisch gefüllt -->
            </div>
        </div>
    `;

    // Initialisierung
    function init() {
        // Navbar in den Body einfügen
        const navPlaceholder = document.getElementById('navbar-placeholder');
        if (navPlaceholder) {
            navPlaceholder.innerHTML = navbarHTML;
        } else {
            document.body.insertAdjacentHTML('afterbegin', navbarHTML);
        }

        // Mobile Menu Overlay hinzufügen
        document.body.insertAdjacentHTML('beforeend', mobileMenuHTML);

        // Nach kurzer Verzögerung initialisieren
        setTimeout(() => {
            setupMobileMenu();
            setupDropdowns();
            loadLearningAreas();
            updateNavigation();
            setupAuthListener();
        }, 100);
    }

    // Mobile Menu Setup
    function setupMobileMenu() {
        const toggle = document.getElementById('mobileMenuToggle');
        const overlay = document.getElementById('mobileMenuOverlay');
        const close = document.getElementById('mobileMenuClose');

        if (!toggle || !overlay || !close) return;

        toggle.addEventListener('click', () => {
            overlay.classList.add('active');
            document.body.style.overflow = 'hidden';
            renderMobileMenu();
        });

        close.addEventListener('click', closeMobileMenu);

        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeMobileMenu();
            }
        });
    }

    function closeMobileMenu() {
        const overlay = document.getElementById('mobileMenuOverlay');
        if (overlay) {
            overlay.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    function renderMobileMenu() {
        const content = document.getElementById('mobileMenuContent');
        if (!content) return;

        const isLoggedIn = window.auth?.isLoggedIn();
        const user = isLoggedIn ? window.auth.getUser() : null;
        const isAdmin = user && window.auth.hasRole('ADMIN');

        let menuHTML = `
            <div class="mobile-menu-section">
                <a href="/userpages/user-home.html" class="mobile-menu-item">
                    <span class="mobile-menu-icon"></span>
                    <span>Home</span>
                </a>
                <a href="/lernbereiche/table-of-contents.html" class="mobile-menu-item">
                    <span class="mobile-menu-icon"></span>
                    <span>Inhaltsverzeichnis</span>
                </a>
                <a href="/quiz/quizzes.html" class="mobile-menu-item">
                    <span class="mobile-menu-icon"></span>
                    <span>Quizzes</span>
                </a>
            </div>
        `;

        if (isLoggedIn && user) {
            menuHTML += `
                <div class="mobile-menu-divider"></div>
                <div class="mobile-menu-section">
                    <div class="mobile-user-info">
                        <div class="mobile-user-avatar">
                            ${user.fullname ? user.fullname.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <div class="mobile-user-details">
                            <div class="mobile-user-name">${user.fullname || 'Benutzer'}</div>
                            <div class="mobile-user-email">${user.email || ''}</div>
                        </div>
                    </div>
                    <a href="/userpages/user-home.html" class="mobile-menu-item">
                        <span class="mobile-menu-icon"></span>
                        <span>Mein Dashboard</span>
                    </a>
                    <a href="/userpages/account.html" class="mobile-menu-item">
                        <span class="mobile-menu-icon">⚙</span>
                        <span>Mein Account</span>
                    </a>
                    ${isAdmin ? `
                        <a href="/admin/admin-dashboard.html" class="mobile-menu-item mobile-menu-admin">
                            <span class="mobile-menu-icon"></span>
                            <span>Admin Dashboard</span>
                        </a>
                    ` : ''}
                    <button onclick="window.auth.logout('/index.html')" class="mobile-menu-item mobile-menu-logout">
                        <span class="mobile-menu-icon"></span>
                        <span>Abmelden</span>
                    </button>
                </div>
            `;
        } else {
            menuHTML += `
                <div class="mobile-menu-divider"></div>
                <div class="mobile-menu-section">
                    <a href="/auth/login.html" class="mobile-menu-item">
                        <span class="mobile-menu-icon"></span>
                        <span>Anmelden</span>
                    </a>
                    <a href="/auth/register.html" class="mobile-menu-item mobile-menu-primary">
                        <span class="mobile-menu-icon"></span>
                        <span>Registrieren</span>
                    </a>
                </div>
            `;
        }

        content.innerHTML = menuHTML;
    }

    // Dropdown Setup
    function setupDropdowns() {
        let dropdownTimeout = null;

        const navDropdowns = document.querySelectorAll('.nav-item.dropdown');
        navDropdowns.forEach(dropdown => {
            const menu = dropdown.querySelector('.dropdown-menu');
            if (!menu) return;

            dropdown.addEventListener('mouseenter', function() {
                clearTimeout(dropdownTimeout);
                menu.style.display = 'block';
            });

            dropdown.addEventListener('mouseleave', function() {
                dropdownTimeout = setTimeout(() => {
                    menu.style.display = 'none';
                }, 300);
            });

            menu.addEventListener('mouseenter', function() {
                clearTimeout(dropdownTimeout);
            });

            menu.addEventListener('mouseleave', function() {
                dropdownTimeout = setTimeout(() => {
                    menu.style.display = 'none';
                }, 300);
            });
        });

        // Click outside to close
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.dropdown') && !e.target.closest('.nav-profile-dropdown')) {
                document.querySelectorAll('.dropdown-menu, .nav-profile-menu').forEach(menu => {
                    menu.style.display = 'none';
                });
            }
        });
    }

    // Lernbereiche laden
    async function loadLearningAreas() {
        try {
            const response = await fetch('http://localhost:8080/api/learning-areas');
            if (!response.ok) return;

            const areas = await response.json();
            const menu = document.getElementById('learningAreasMenu');

            if (!menu || areas.length === 0) return;

            const publishedAreas = areas.filter(a => a.isPublished);

            if (publishedAreas.length > 0) {
                menu.innerHTML = publishedAreas
                    .sort((a, b) => a.displayOrder - b.displayOrder)
                    .map(area => `
                        <li>
                            <a href="/lernbereiche/learning-area.html?area=${area.slug}" class="dropdown-item">
                                ${area.name}
                            </a>
                        </li>
                    `).join('') + `
                    
                    
                `;
            }
        } catch (error) {
            console.error('Fehler beim Laden der Lernbereiche:', error);
        }
    }

    // Navigation Update basierend auf Auth-Status
    function updateNavigation() {
        const navRight = document.getElementById('navRight');
        if (!navRight) return;

        const isLoggedIn = window.auth?.isLoggedIn();

        if (!isLoggedIn) {
            navRight.innerHTML = `
                <a href="/auth/login.html" class="btn-login">Anmelden</a>
                <a href="/auth/register.html" class="btn btn-primary">Registrieren</a>
            `;
            return;
        }

        const user = window.auth.getUser();
        const displayName = user.fullname ? user.fullname.split(' ')[0] : 'Benutzer';
        const isAdmin = window.auth.hasRole('ADMIN');

        navRight.innerHTML = `
            <div class="nav-profile-dropdown">
                <button class="nav-profile-btn">
                    <span class="nav-profile-avatar">
                        ${displayName.charAt(0).toUpperCase()}
                    </span>
                    <span class="nav-profile-name">${displayName}</span>
                    <span class="nav-profile-arrow">▾</span>
                </button>
                <div class="nav-profile-menu">
                    <div class="nav-profile-header">
                        <div class="nav-profile-info">
                            <div class="nav-profile-fullname">${user.fullname || 'Benutzer'}</div>
                            <div class="nav-profile-email">${user.email || ''}</div>
                        </div>
                    </div>
                    <div class="nav-profile-divider"></div>
                    <a href="/userpages/user-home.html" class="nav-profile-item">
                        <span class="nav-profile-icon"></span>
                        <span>Dashboard</span>
                    </a>
                    <a href="/userpages/account.html" class="nav-profile-item">
                        <span class="nav-profile-icon"></span>
                        <span>Mein Account</span>
                    </a>
                    ${isAdmin ? `
                        <div class="nav-profile-divider"></div>
                        <a href="/admin/admin-dashboard.html" class="nav-profile-item nav-profile-admin">
                            <span class="nav-profile-icon"></span>
                            <span>Admin Dashboard</span>
                        </a>
                    ` : ''}
                    <div class="nav-profile-divider"></div>
                    <button onclick="window.auth.logout('/index.html')" class="nav-profile-item nav-profile-logout">
                        <span class="nav-profile-icon"></span>
                        <span>Abmelden</span>
                    </button>
                </div>
            </div>
        `;

        // Setup Profile Dropdown
        setupProfileDropdown();
    }

    // Profile Dropdown Setup
    function setupProfileDropdown() {
        let dropdownTimeout = null;
        const profileDropdown = document.querySelector('.nav-profile-dropdown');

        if (!profileDropdown) return;

        const profileMenu = profileDropdown.querySelector('.nav-profile-menu');
        const profileBtn = profileDropdown.querySelector('.nav-profile-btn');

        if (!profileMenu || !profileBtn) return;

        profileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const isVisible = profileMenu.style.display === 'block';

            // Alle anderen Menüs schließen
            document.querySelectorAll('.dropdown-menu, .nav-profile-menu').forEach(menu => {
                menu.style.display = 'none';
            });

            profileMenu.style.display = isVisible ? 'none' : 'block';
        });

        profileDropdown.addEventListener('mouseenter', function() {
            clearTimeout(dropdownTimeout);
            profileMenu.style.display = 'block';
        });

        profileDropdown.addEventListener('mouseleave', function() {
            dropdownTimeout = setTimeout(() => {
                profileMenu.style.display = 'none';
            }, 300);
        });

        profileMenu.addEventListener('mouseenter', function() {
            clearTimeout(dropdownTimeout);
        });

        profileMenu.addEventListener('mouseleave', function() {
            dropdownTimeout = setTimeout(() => {
                profileMenu.style.display = 'none';
            }, 300);
        });
    }

    // Auth State Listener
    function setupAuthListener() {
        window.addEventListener('authStateChanged', () => {
            updateNavigation();
        });

        window.addEventListener('storage', (e) => {
            if (e.key === 'authToken') {
                updateNavigation();
            }
        });
    }

    // Expose global update function
    window.updateNavigation = updateNavigation;

    // Auto-Init
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();