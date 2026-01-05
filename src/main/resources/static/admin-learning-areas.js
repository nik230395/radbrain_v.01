// admin-learning-areas.js
// Verwaltung von Lernbereichen im Admin-Panel

const API_BASE = 'http://localhost:8080/api';
let currentAreas = [];
let editingAreaId = null;

// ============================================================
// Initialisierung
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    checkAdminAuth();
    loadAreas();
});

// ============================================================
// API-Funktionen
// ============================================================

async function loadAreas() {
    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Laden');

        currentAreas = await response.json();
        renderAreas();
    } catch (error) {
        console.error('Fehler beim Laden der Lernbereiche:', error);
        showError('Lernbereiche konnten nicht geladen werden');
    }
}

async function saveArea(event) {
    event.preventDefault();

    const areaData = {
        name: document.getElementById('areaName').value,
        slug: document.getElementById('areaSlug').value,
        subtitle: document.getElementById('areaSubtitle').value,
        description: document.getElementById('areaDescription').value,
        iconClass: document.getElementById('areaIconClass').value,
        colorTheme: document.getElementById('areaColorTheme').value,
        displayOrder: parseInt(document.getElementById('areaDisplayOrder').value),
        isPublished: document.getElementById('areaIsPublished').checked
    };

    try {
        const token = localStorage.getItem('authToken');
        const url = editingAreaId
            ? `${API_BASE}/admin/learning-areas/${editingAreaId}`
            : `${API_BASE}/admin/learning-areas`;

        const method = editingAreaId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(areaData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Fehler beim Speichern');
        }

        closeModal();
        await loadAreas();
        showSuccess(editingAreaId ? 'Lernbereich aktualisiert' : 'Lernbereich erstellt');
    } catch (error) {
        console.error('Fehler beim Speichern:', error);
        showError(error.message || 'Lernbereich konnte nicht gespeichert werden');
    }
}

async function deleteArea(areaId, areaName) {
    if (!confirm(`Möchtest du "${areaName}" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.`)) {
        return;
    }

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas/${areaId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Löschen');

        await loadAreas();
        showSuccess('Lernbereich gelöscht');
    } catch (error) {
        console.error('Fehler beim Löschen:', error);
        showError('Lernbereich konnte nicht gelöscht werden');
    }
}

async function togglePublished(areaId) {
    const area = currentAreas.find(a => a.id === areaId);
    if (!area) return;

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas/${areaId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                ...area,
                isPublished: !area.isPublished
            })
        });

        if (!response.ok) throw new Error('Fehler beim Aktualisieren');

        await loadAreas();
        showSuccess(area.isPublished ? 'Lernbereich versteckt' : 'Lernbereich veröffentlicht');
    } catch (error) {
        console.error('Fehler beim Aktualisieren:', error);
        showError('Status konnte nicht geändert werden');
    }
}

// ============================================================
// UI-Funktionen
// ============================================================

function renderAreas() {
    const container = document.getElementById('areasContainer');
    const emptyState = document.getElementById('emptyState');

    if (currentAreas.length === 0) {
        container.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    container.style.display = 'grid';
    emptyState.style.display = 'none';

    container.innerHTML = currentAreas
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(area => createAreaCard(area))
        .join('');
}

function createAreaCard(area) {
    return `
        <div class="area-card" style="border-left: 4px solid ${area.colorTheme || '#3b82f6'}">
            <div class="area-header">
                <div>
                    <h3 class="area-title">${escapeHtml(area.name)}</h3>
                    <span class="area-slug">/${area.slug}</span>
                </div>
                <span class="area-status ${area.isPublished ? 'status-published' : 'status-draft'}">
                    ${area.isPublished ? '✓ Veröffentlicht' : '○ Entwurf'}
                </span>
            </div>

            ${area.subtitle ? `<p style="color: #6b7280; font-size: 0.875rem; margin: 0.5rem 0;">${escapeHtml(area.subtitle)}</p>` : ''}

            ${area.description ? `
                <p class="area-description">
                    ${escapeHtml(area.description).substring(0, 150)}${area.description.length > 150 ? '...' : ''}
                </p>
            ` : ''}

            <div class="area-stats">
                <div class="stat">
                    <span class="stat-label">Module</span>
                    <span class="stat-value">${area.moduleCount || 0}</span>
                </div>
                <div class="stat">
                    <span class="stat-label">Position</span>
                    <span class="stat-value">${area.displayOrder}</span>
                </div>
            </div>

            <div class="area-actions">
                <button class="btn-edit" onclick="editArea(${area.id})">
                    Bearbeiten
                </button>
                <button class="btn-toggle" onclick="togglePublished(${area.id})">
                    ${area.isPublished ? 'Verstecken' : 'Veröffentlichen'}
                </button>
                <button class="btn-edit" onclick="openModuleEditor(${area.id}, '${escapeHtml(area.name)}')">
                    Module
                </button>
                <button class="btn-delete" onclick="deleteArea(${area.id}, '${escapeHtml(area.name)}')">
                    Löschen
                </button>
            </div>
        </div>
    `;
}

function openCreateModal() {
    editingAreaId = null;
    document.getElementById('modalTitle').textContent = 'Neuer Lernbereich';
    document.getElementById('areaForm').reset();
    document.getElementById('areaColorTheme').value = '#3b82f6';
    document.getElementById('areaDisplayOrder').value = '0';
    document.getElementById('areaModal').classList.add('active');
}

function editArea(areaId) {
    const area = currentAreas.find(a => a.id === areaId);
    if (!area) return;

    editingAreaId = areaId;
    document.getElementById('modalTitle').textContent = 'Lernbereich bearbeiten';
    document.getElementById('areaId').value = area.id;
    document.getElementById('areaName').value = area.name;
    document.getElementById('areaSlug').value = area.slug;
    document.getElementById('areaSubtitle').value = area.subtitle || '';
    document.getElementById('areaDescription').value = area.description || '';
    document.getElementById('areaIconClass').value = area.iconClass || '';
    document.getElementById('areaColorTheme').value = area.colorTheme || '#3b82f6';
    document.getElementById('areaDisplayOrder').value = area.displayOrder || 0;
    document.getElementById('areaIsPublished').checked = area.isPublished || false;

    document.getElementById('areaModal').classList.add('active');
}

function closeModal() {
    document.getElementById('areaModal').classList.remove('active');
    editingAreaId = null;
}

function openModuleEditor(areaId, areaName) {
    // Zu Modul-Editor navigieren
    window.location.href = `/admin-modules.html?areaId=${areaId}`;
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
    alert(message); // TODO: Bessere Notification
}

function showError(message) {
    alert('Fehler: ' + message); // TODO: Bessere Notification
}

// Modal schließen bei Klick außerhalb
document.addEventListener('click', (e) => {
    const modal = document.getElementById('areaModal');
    if (e.target === modal) {
        closeModal();
    }
});

// Slug automatisch aus Name generieren
document.getElementById('areaName')?.addEventListener('input', (e) => {
    if (!editingAreaId) { // Nur bei neuem Lernbereich
        const slug = e.target.value
            .toLowerCase()
            .replace(/ä/g, 'ae')
            .replace(/ö/g, 'oe')
            .replace(/ü/g, 'ue')
            .replace(/ß/g, 'ss')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');
        document.getElementById('areaSlug').value = slug;
    }
});