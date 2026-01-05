// admin-modules.js
// Verwaltung von Modulen innerhalb eines Lernbereichs

const API_BASE = 'http://localhost:8080/api';
let currentAreaId = null;
let currentModules = [];
let editingModuleId = null;

// ============================================================
// Initialisierung
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    checkAdminAuth();

    const urlParams = new URLSearchParams(window.location.search);
    currentAreaId = urlParams.get('areaId');

    if (!currentAreaId) {
        alert('Kein Lernbereich ausgewählt');
        window.location.href = '/admin-learning-areas.html';
        return;
    }

    loadArea();
    loadModules();
});

// ============================================================
// API-Funktionen
// ============================================================

async function loadArea() {
    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Laden');

        const areas = await response.json();
        const area = areas.find(a => a.id == currentAreaId);

        if (area) {
            document.getElementById('areaName').textContent = area.name;
        }
    } catch (error) {
        console.error('Fehler beim Laden des Bereichs:', error);
    }
}

async function loadModules() {
    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/learning-areas/${getAreaSlug()}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Laden');

        const area = await response.json();
        currentModules = area.modules || [];

        renderModules();
    } catch (error) {
        console.error('Fehler beim Laden der Module:', error);
        showError('Module konnten nicht geladen werden');
    }
}

async function getAreaSlug() {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE}/admin/learning-areas`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const areas = await response.json();
    const area = areas.find(a => a.id == currentAreaId);
    return area ? area.slug : '';
}

async function saveModule(event) {
    event.preventDefault();

    const moduleData = {
        title: document.getElementById('moduleTitle').value,
        subtitle: document.getElementById('moduleSubtitle').value,
        description: document.getElementById('moduleDescription').value,
        icon: document.getElementById('moduleIcon').value,
        estimatedDuration: parseInt(document.getElementById('moduleDuration').value) || null,
        displayOrder: parseInt(document.getElementById('moduleOrder').value) || 0
    };

    try {
        const token = localStorage.getItem('authToken');
        const url = editingModuleId
            ? `${API_BASE}/admin/learning-areas/modules/${editingModuleId}`
            : `${API_BASE}/admin/learning-areas/${currentAreaId}/modules`;

        const method = editingModuleId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(moduleData)
        });

        if (!response.ok) {
            throw new Error('Fehler beim Speichern');
        }

        closeModuleModal();
        await loadModules();
        showSuccess(editingModuleId ? 'Modul aktualisiert' : 'Modul erstellt');
    } catch (error) {
        console.error('Fehler beim Speichern:', error);
        showError('Modul konnte nicht gespeichert werden');
    }
}

async function deleteModule(moduleId, moduleTitle) {
    if (!confirm(`Möchtest du "${moduleTitle}" wirklich löschen? Alle Inhalte gehen verloren!`)) {
        return;
    }

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas/modules/${moduleId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Löschen');

        await loadModules();
        showSuccess('Modul gelöscht');
    } catch (error) {
        console.error('Fehler beim Löschen:', error);
        showError('Modul konnte nicht gelöscht werden');
    }
}

// ============================================================
// UI-Funktionen
// ============================================================

function renderModules() {
    const list = document.getElementById('modulesList');
    const emptyState = document.getElementById('emptyState');

    if (currentModules.length === 0) {
        list.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    list.style.display = 'block';
    emptyState.style.display = 'none';

    list.innerHTML = currentModules
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(module => createModuleItem(module))
        .join('');
}

function createModuleItem(module) {
    return `
        <div class="module-item">
            <div class="drag-handle">☰</div>
            <div class="module-info">
                <div class="module-title">
                    ${module.icon ? module.icon + ' ' : ''}${escapeHtml(module.title)}
                </div>
                <div class="module-subtitle">${escapeHtml(module.subtitle || '')}</div>
                <div class="module-meta">
                    <span>📄 ${module.contentCount || 0} Inhalte</span>
                    ${module.estimatedDuration ? `<span>⏱️ ${module.estimatedDuration} Min</span>` : ''}
                    <span>Position: ${module.displayOrder}</span>
                </div>
            </div>
            <div class="module-actions">
                <button class="btn-content" onclick="openContentEditor(${module.id}, '${escapeHtml(module.title)}')">
                    Inhalte (${module.contentCount || 0})
                </button>
                <button class="btn-edit" onclick="editModule(${module.id})">
                    Bearbeiten
                </button>
                <button class="btn-delete" onclick="deleteModule(${module.id}, '${escapeHtml(module.title)}')">
                    Löschen
                </button>
            </div>
        </div>
    `;
}

function openCreateModuleModal() {
    editingModuleId = null;
    document.getElementById('moduleModalTitle').textContent = 'Neues Modul';
    document.getElementById('moduleForm').reset();
    document.getElementById('moduleOrder').value = currentModules.length;
    document.getElementById('moduleModal').classList.add('active');
}

function editModule(moduleId) {
    const module = currentModules.find(m => m.id === moduleId);
    if (!module) return;

    editingModuleId = moduleId;
    document.getElementById('moduleModalTitle').textContent = 'Modul bearbeiten';
    document.getElementById('moduleId').value = module.id;
    document.getElementById('moduleTitle').value = module.title;
    document.getElementById('moduleSubtitle').value = module.subtitle || '';
    document.getElementById('moduleDescription').value = module.description || '';
    document.getElementById('moduleIcon').value = module.icon || '';
    document.getElementById('moduleDuration').value = module.estimatedDuration || '';
    document.getElementById('moduleOrder').value = module.displayOrder || 0;

    document.getElementById('moduleModal').classList.add('active');
}

function closeModuleModal() {
    document.getElementById('moduleModal').classList.remove('active');
    editingModuleId = null;
}

function openContentEditor(moduleId, moduleTitle) {
    window.location.href = `/admin-content.html?moduleId=${moduleId}&title=${encodeURIComponent(moduleTitle)}&areaId=${currentAreaId}`;
}

// ============================================================
// Helper-Funktionen
// ============================================================

function checkAdminAuth() {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');

    if (!token || userRole !== 'ADMIN') {
        window.location.href = '/admin.html';
        return;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showSuccess(message) {
    alert(message);
}

function showError(message) {
    alert('Fehler: ' + message);
}

// Modal schließen bei Klick außerhalb
document.addEventListener('click', (e) => {
    const modal = document.getElementById('moduleModal');
    if (e.target === modal) {
        closeModuleModal();
    }
});