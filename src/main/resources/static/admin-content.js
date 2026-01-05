const API_BASE = 'http://localhost:8080/api';
let currentModuleId = null;
let currentContents = [];
let editingContentId = null;


const CONTENT_TYPE_INFO = {
    TEXT: {
        title: 'Text',
        description: 'Normaler Fließtext für Erklärungen und Beschreibungen.',
        example: 'Dies ist ein normaler Text...'
    },
    HEADING: {
        title: 'Überschrift',
        description: 'Große Überschrift für Abschnitte.',
        example: 'Wichtiges Thema'
    },
    LIST: {
        title: 'Liste',
        description: 'JSON-Array für Aufzählungen.',
        example: '["Punkt 1", "Punkt 2", "Punkt 3"]'
    },
    CALLOUT: {
        title: 'Hinweisbox',
        description: 'Hervorgehobener Hinweis oder wichtige Information.',
        example: 'Wichtig: Dies solltest du beachten!'
    },
    QUOTE: {
        title: 'Zitat',
        description: 'Zitat oder hervorgehobener Text.',
        example: 'Ein wichtiges Zitat...'
    },
    CODE: {
        title: 'Code',
        description: 'Code-Beispiel mit Monospace-Schrift.',
        example: 'function hello() {\n  console.log("Hello!");\n}'
    },
    IMAGE: {
        title: 'Bild',
        description: 'JSON mit url und caption.',
        example: '{"url": "https://...", "caption": "Bildbeschreibung"}'
    },
    TABLE: {
        title: 'Tabelle',
        description: 'JSON mit headers und rows Arrays.',
        example: '{"headers": ["Spalte 1", "Spalte 2"], "rows": [["Wert 1", "Wert 2"]]}'
    }
};

document.addEventListener('DOMContentLoaded', async () => {
    checkAdminAuth();

    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');
    const moduleTitle = urlParams.get('title');

    if (!currentModuleId) {
        alert('Kein Modul ausgewählt');
        window.location.href = '/admin-learning-areas.html';
        return;
    }

    document.getElementById('moduleTitle').textContent = decodeURIComponent(moduleTitle || 'Modul');

    // Back-Link vorbereiten
    const areaId = await getAreaIdFromModule();
    document.getElementById('backToModules').href = `/admin-modules.html?areaId=${areaId}`;

    loadContents();
    updateContentTypeInfo();
});


async function loadContents() {
    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/learning-areas/modules/${currentModuleId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Laden');

        const module = await response.json();
        currentContents = module.contents || [];

        renderContents();
    } catch (error) {
        console.error('Fehler beim Laden der Inhalte:', error);
        showError('Inhalte konnten nicht geladen werden');
    }
}

async function getAreaIdFromModule() {
    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/learning-areas/modules/${currentModuleId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) return '';
        const module = await response.json();
        return module.learningAreaId || '';
    } catch {
        return '';
    }
}

async function saveContent(event) {
    event.preventDefault();

    const contentData = {
        contentType: document.getElementById('contentType').value,
        title: document.getElementById('contentTitle').value || null,
        contentData: document.getElementById('contentData').value,
        displayOrder: parseInt(document.getElementById('contentOrder').value) || 0,
        metadata: null
    };

    try {
        const token = localStorage.getItem('authToken');
        const url = editingContentId
            ? `${API_BASE}/admin/learning-areas/contents/${editingContentId}`
            : `${API_BASE}/admin/learning-areas/modules/${currentModuleId}/contents`;

        const method = editingContentId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(contentData)
        });

        if (!response.ok) {
            throw new Error('Fehler beim Speichern');
        }

        closeContentModal();
        await loadContents();
        showSuccess(editingContentId ? 'Inhalt aktualisiert' : 'Inhalt erstellt');
    } catch (error) {
        console.error('Fehler beim Speichern:', error);
        showError('Inhalt konnte nicht gespeichert werden');
    }
}

async function deleteContent(contentId) {
    if (!confirm('Möchtest du diesen Inhalt wirklich löschen?')) {
        return;
    }

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_BASE}/admin/learning-areas/contents/${contentId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Fehler beim Löschen');

        await loadContents();
        showSuccess('Inhalt gelöscht');
    } catch (error) {
        console.error('Fehler beim Löschen:', error);
        showError('Inhalt konnte nicht gelöscht werden');
    }
}

// ============================================================
// UI-Funktionen
// ============================================================

function renderContents() {
    const list = document.getElementById('contentList');
    const emptyState = document.getElementById('emptyState');

    if (currentContents.length === 0) {
        list.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    list.style.display = 'block';
    emptyState.style.display = 'none';

    list.innerHTML = currentContents
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .map(content => createContentItem(content))
        .join('');
}

function createContentItem(content) {
    const preview = getContentPreview(content);

    return `
        <div class="content-item">
            <div class="drag-handle">☰</div>
            <span class="content-type-badge type-${content.contentType}">
                ${content.contentType}
            </span>
            <div class="content-preview">
                ${content.title ? `<div class="content-title">${escapeHtml(content.title)}</div>` : ''}
                <div class="content-data">${preview}</div>
            </div>
            <div class="content-actions">
                <button class="btn-edit" onclick="editContent(${content.id})">
                    Bearbeiten
                </button>
                <button class="btn-delete" onclick="deleteContent(${content.id})">
                    Löschen
                </button>
            </div>
        </div>
    `;
}

function getContentPreview(content) {
    let data = content.contentData;
    if (!data) return '';

    // Bei JSON versuchen zu formatieren
    try {
        const parsed = JSON.parse(data);
        if (Array.isArray(parsed)) {
            return escapeHtml(parsed.join(', '));
        } else if (typeof parsed === 'object') {
            return escapeHtml(JSON.stringify(parsed).substring(0, 100));
        }
    } catch {
        // Kein JSON, normaler Text
    }

    return escapeHtml(data.substring(0, 100)) + (data.length > 100 ? '...' : '');
}

function openCreateContentModal() {
    editingContentId = null;
    document.getElementById('contentModalTitle').textContent = 'Neuer Inhalt';
    document.getElementById('contentForm').reset();
    document.getElementById('contentType').value = 'TEXT';
    document.getElementById('contentOrder').value = currentContents.length;
    updateContentTypeInfo();
    document.getElementById('contentModal').classList.add('active');
}

function editContent(contentId) {
    const content = currentContents.find(c => c.id === contentId);
    if (!content) return;

    editingContentId = contentId;
    document.getElementById('contentModalTitle').textContent = 'Inhalt bearbeiten';
    document.getElementById('contentId').value = content.id;
    document.getElementById('contentType').value = content.contentType;
    document.getElementById('contentTitle').value = content.title || '';
    document.getElementById('contentData').value = content.contentData || '';
    document.getElementById('contentOrder').value = content.displayOrder || 0;

    updateContentTypeInfo();
    document.getElementById('contentModal').classList.add('active');
}

function closeContentModal() {
    document.getElementById('contentModal').classList.remove('active');
    editingContentId = null;
}

function updateContentTypeInfo() {
    const type = document.getElementById('contentType').value;
    const info = CONTENT_TYPE_INFO[type];

    if (info) {
        document.getElementById('contentTypeInfo').innerHTML = `
            <h4>${info.title}</h4>
            <p>${info.description}</p>
            <p><strong>Beispiel:</strong> <code>${escapeHtml(info.example)}</code></p>
        `;
    }
}

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
    const modal = document.getElementById('contentModal');
    if (e.target === modal) {
        closeContentModal();
    }
});