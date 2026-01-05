// learning-area-new.js
// Lädt Lernbereich dynamisch und zeigt Content UNTEN (nicht im Modal)

const API_BASE = 'http://localhost:8080/api';
let currentArea = null;
let currentModules = [];

// ============================================================
// Initialisierung
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const slug = urlParams.get('area');

    if (slug) {
        loadLearningArea(slug);
    } else {
        showError();
    }

    loadNavigationAreas();
    setupScrollHandlers();
});

// ============================================================
// API-Funktionen
// ============================================================

async function loadLearningArea(slug) {
    try {
        showLoading();

        const response = await fetch(`${API_BASE}/learning-areas/${slug}`);

        if (!response.ok) {
            throw new Error('Lernbereich nicht gefunden');
        }

        currentArea = await response.json();
        currentModules = currentArea.modules || [];

        // Lade Content für alle Module
        await loadAllModuleContents();

        renderPage();
        hideLoading();

        // Smooth Scroll nach kurzer Verzögerung
        setTimeout(setupSmoothScroll, 500);

    } catch (error) {
        console.error('Fehler beim Laden:', error);
        showError();
    }
}

async function loadAllModuleContents() {
    // Lade Content für alle Module parallel
    const promises = currentModules.map(module => loadModuleContent(module.id));
    const results = await Promise.all(promises);

    // Content zu Modulen hinzufügen
    results.forEach((moduleWithContent, index) => {
        if (moduleWithContent && moduleWithContent.contents) {
            currentModules[index].contents = moduleWithContent.contents;
        }
    });
}

async function loadModuleContent(moduleId) {
    try {
        const response = await fetch(`${API_BASE}/learning-areas/modules/${moduleId}`);
        if (!response.ok) return null;
        return await response.json();
    } catch (error) {
        console.error('Fehler beim Laden des Moduls:', error);
        return null;
    }
}

async function loadNavigationAreas() {
    try {
        const response = await fetch(`${API_BASE}/learning-areas`);
        if (!response.ok) return;

        const areas = await response.json();
        renderNavigationDropdown(areas);
    } catch (error) {
        console.error('Fehler beim Laden der Navigation:', error);
    }
}

// ============================================================
// Rendering-Funktionen
// ============================================================

function renderPage() {
    // Hero Section
    document.getElementById('pageTitle').textContent = `${currentArea.name} - RadBrain`;
    document.getElementById('heroTitle').textContent = currentArea.name;
    document.getElementById('heroSubtitle').textContent = currentArea.subtitle || currentArea.description;

    // Hero Background
    const heroSection = document.getElementById('heroSection');
    if (currentArea.colorTheme) {
        heroSection.style.background = `linear-gradient(135deg, ${currentArea.colorTheme} 0%, ${adjustColor(currentArea.colorTheme, -20)} 100%)`;
    }

    // Quiz Link
    document.getElementById('quizLink').href = `/quiz.html?category=${currentArea.slug}`;

    // Inhaltsverzeichnis (Karten)
    renderTocCards();

    // Sticky TOC Liste
    renderStickyToc();

    // Alle Module mit Content
    renderAllModules();

    document.getElementById('mainContent').style.display = 'block';
}

function renderTocCards() {
    const grid = document.getElementById('modulesGrid');

    if (!currentModules || currentModules.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 3rem;">
                <p style="color: #6b7280; font-size: 1.125rem;">Noch keine Module verfügbar.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = currentModules
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map((module, index) => `
            <a href="#module-${module.id}" class="toc-card">
                <div class="toc-number">${String(index + 1).padStart(2, '0')}</div>
                <h3>${module.icon ? module.icon + ' ' : ''}${escapeHtml(module.title)}</h3>
                <p>${escapeHtml(module.subtitle || module.description || '')}</p>
            </a>
        `)
        .join('');
}

function renderStickyToc() {
    const list = document.getElementById('stickyTocList');

    list.innerHTML = currentModules
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(module => `
            <li><a href="#module-${module.id}" class="toc-link">${escapeHtml(module.title)}</a></li>
        `)
        .join('');
}

function renderAllModules() {
    const container = document.getElementById('mainContentArea');

    const modulesHtml = currentModules
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map((module, index) => renderModule(module, index + 1))
        .join('');

    container.innerHTML = modulesHtml;
}

function renderModule(module, number) {
    const contents = module.contents || [];

    if (contents.length === 0) {
        return `
            <section id="module-${module.id}" class="content-section">
                <div class="content-header">
                    <span class="content-badge">Kapitel ${number}</span>
                    <h2>${module.icon ? module.icon + ' ' : ''}${escapeHtml(module.title)}</h2>
                </div>
                <div class="content-grid">
                    <div class="content-text">
                        <p style="color: #6b7280;">Noch keine Inhalte verfügbar.</p>
                    </div>
                </div>
            </section>
        `;
    }

    return `
        <section id="module-${module.id}" class="content-section">
            <div class="content-header">
                <span class="content-badge">Kapitel ${number}</span>
                <h2>${module.icon ? module.icon + ' ' : ''}${escapeHtml(module.title)}</h2>
            </div>
            <div class="content-grid">
                <div class="content-text">
                    ${renderContents(contents)}
                </div>
            </div>
        </section>
    `;
}

function renderContents(contents) {
    return contents
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(content => renderContent(content))
        .join('');
}

function renderContent(content) {
    const type = content.contentType;
    const data = content.contentData;
    const title = content.title;

    switch(type) {
        case 'HEADING':
            return `<h3>${escapeHtml(title || data)}</h3>`;

        case 'TEXT':
            return `
                ${title ? `<h4 style="font-weight: 600; margin-top: 2rem; margin-bottom: 0.5rem;">${escapeHtml(title)}</h4>` : ''}
                <p>${escapeHtml(data)}</p>
            `;

        case 'CALLOUT':
        case 'INFO':
            return `
                <div class="info-box" style="margin-top: 2rem;">
                    ${title ? `<h4>${escapeHtml(title)}</h4>` : ''}
                    <p>${escapeHtml(data)}</p>
                </div>
            `;

        case 'QUOTE':
            return `
                <blockquote style="border-left: 4px solid #d1d5db; padding-left: 1.5rem; font-style: italic; color: #4b5563; margin: 2rem 0;">
                    ${escapeHtml(data)}
                    ${title ? `<br><small>— ${escapeHtml(title)}</small>` : ''}
                </blockquote>
            `;

        case 'LIST':
            try {
                const items = JSON.parse(data);
                return `
                    ${title ? `<h4 style="font-weight: 600; margin-top: 2rem; margin-bottom: 0.5rem;">${escapeHtml(title)}</h4>` : ''}
                    <ul style="margin: 1rem 0; padding-left: 1.5rem;">
                        ${items.map(item => `<li style="margin-bottom: 0.5rem;">${escapeHtml(item)}</li>`).join('')}
                    </ul>
                `;
            } catch {
                return `<p>${escapeHtml(data)}</p>`;
            }

        case 'CODE':
            return `
                ${title ? `<h4 style="font-weight: 600; margin-top: 2rem; margin-bottom: 0.5rem;">${escapeHtml(title)}</h4>` : ''}
                <pre style="background: #1f2937; color: #f3f4f6; padding: 1rem; border-radius: 6px; overflow-x: auto; margin: 1rem 0;"><code>${escapeHtml(data)}</code></pre>
            `;

        default:
            return `<p>${escapeHtml(data)}</p>`;
    }
}

function renderNavigationDropdown(areas) {
    const dropdown = document.getElementById('learningAreasDropdown');

    dropdown.innerHTML = areas
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(area => `
            <li><a href="/learning-area.html?area=${area.slug}">${escapeHtml(area.name)}</a></li>
        `)
        .join('');
}

// ============================================================
// Scroll & Navigation
// ============================================================

function setupScrollHandlers() {
    // Intersection Observer für aktives Kapitel
    const observerOptions = {
        root: null,
        rootMargin: '-90px 0px -60% 0px',
        threshold: [0, 0.1, 0.25, 0.5, 0.75, 1]
    };

    const observer = new IntersectionObserver((entries) => {
        let maxRatio = 0;
        let activeEntry = null;

        entries.forEach(entry => {
            if (entry.isIntersecting && entry.intersectionRatio > maxRatio) {
                maxRatio = entry.intersectionRatio;
                activeEntry = entry;
            }
        });

        if (activeEntry) {
            const id = activeEntry.target.getAttribute('id');
            updateActiveTocLink(id);
        }
    }, observerOptions);

    // Beobachte alle Sections
    document.querySelectorAll('.content-section').forEach(section => {
        observer.observe(section);
    });

    // Progress Bar
    updateProgressBar();
    window.addEventListener('scroll', updateProgressBar);
    window.addEventListener('resize', updateProgressBar);
}

function setupSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href');
            if (targetId === '#') return;

            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                const offset = 90;
                const targetPosition = targetSection.getBoundingClientRect().top + window.pageYOffset - offset;
                window.scrollTo({top: targetPosition, behavior: 'smooth'});
            }
        });
    });
}

function updateActiveTocLink(id) {
    document.querySelectorAll('.sticky-toc-list a').forEach(link => {
        link.classList.remove('active');
    });

    const activeLink = document.querySelector(`.sticky-toc-list a[href="#${id}"]`);
    if (activeLink) {
        activeLink.classList.add('active');

        // Auto-scroll im TOC
        const tocContainer = document.querySelector('.sticky-toc');
        if (tocContainer) {
            const linkTop = activeLink.offsetTop;
            const linkHeight = activeLink.offsetHeight;
            const containerScrollTop = tocContainer.scrollTop;
            const containerHeight = tocContainer.offsetHeight;

            if (linkTop < containerScrollTop + 50) {
                tocContainer.scrollTo({top: linkTop - 50, behavior: 'smooth'});
            } else if (linkTop + linkHeight > containerScrollTop + containerHeight - 50) {
                tocContainer.scrollTo({top: linkTop - containerHeight + linkHeight + 50, behavior: 'smooth'});
            }
        }
    }
}

function updateProgressBar() {
    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrolled = (winScroll / height) * 100;
    const progressBar = document.getElementById('progressBar');
    if (progressBar) {
        progressBar.style.height = scrolled + '%';
    }
}

// ============================================================
// UI-State
// ============================================================

function showLoading() {
    document.getElementById('loadingState').style.display = 'block';
    document.getElementById('errorState').style.display = 'none';
    document.getElementById('mainContent').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loadingState').style.display = 'none';
}

function showError() {
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('errorState').style.display = 'block';
    document.getElementById('mainContent').style.display = 'none';
}

// ============================================================
// Helper
// ============================================================

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function adjustColor(color, amount) {
    const num = parseInt(color.replace('#', ''), 16);
    const r = Math.max(0, Math.min(255, (num >> 16) + amount));
    const g = Math.max(0, Math.min(255, ((num >> 8) & 0x00FF) + amount));
    const b = Math.max(0, Math.min(255, (num & 0x0000FF) + amount));
    return '#' + ((r << 16) | (g << 8) | b).toString(16).padStart(6, '0');
}