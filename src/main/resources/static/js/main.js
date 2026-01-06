// Simplified main.js - remove duplicated functionality
// Remove functions that are now handled by auth.js

// Utility functions
function showLoading(element) {
    if (element) element.classList.add('loading');
}

function hideLoading(element) {
    if (element) element.classList.remove('loading');
}

function showMessage(elementId, message, type = 'info') {
    const el = document.getElementById(elementId);
    if (!el) return;

    el.textContent = message;
    el. className = `message ${type}`;
    el.style.display = 'block';
}

// Enhanced error handling
function handleApiError(error, elementId = null) {
    console.error('API Error:', error);
    const message = error.message || 'Ein Fehler ist aufgetreten';
    if (elementId) showMessage(elementId, message, 'error');
    return message;
}

// Dashboard profile loading
if (document.getElementById("profile")) {
    (async function() {
        if (! window.auth?. isLoggedIn()) {
            window.location.href = '/login.html';
            return;
        }

        try {
            const response = await window.auth.authFetch('/api/users/me');
            if (!response.ok) throw new Error('Failed to load profile');

            const data = await response.json();
            document.getElementById("profile").innerHTML = `
                <div class="profile-info">
                    <h3>${data.fullname}</h3>
                    <p>Email:  ${data.email}</p>
                    <p>Status: <span class="status ${data.is_active ? 'active' :  'inactive'}">${data.is_active ?  'Aktiv' : 'Inaktiv'}</span></p>
                </div>
            `;
        } catch (error) {
            handleApiError(error, 'profile');
        }
    })();
}

// Global quiz start function
window.startQuiz = function(quizId, quizTitle) {
    if (!window.auth?.isLoggedIn()) {
        alert('Bitte melde dich an, um an Quizzes teilzunehmen.');
        window.location.href = '/login. html';
        return;
    }

    window.location.href = `/quiz.html?id=${quizId}`;
};