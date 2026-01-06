// Admin Panel JavaScript
// Handles all admin panel functionality including quiz and user management

const adminPanel = (function() {
    const apiBaseUrl = "/api";
    
    // Initialize admin panel
    function init() {
        // Check if user is admin
        if (!window.auth || !window.auth.isLoggedIn()) {
            alert('Please log in first.');
            window.location.href = '/login.html';
            return;
        }
        
        if (!window.auth.hasRole('ADMIN')) {
            alert('Access denied. Admin privileges required.');
            window.location.href = '/dashboard.html';
            return;
        }
        
        // Load initial data
        loadQuizList();
        loadStatistics();
    }
    
    // Load quiz list
    async function loadQuizList() {
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes`);
            if (!response.ok) {
                throw new Error('Failed to load quizzes');
            }
            const quizzes = await response.json();
            displayQuizzes(quizzes);
        } catch (error) {
            console.error('Error loading quizzes:', error);
            document.getElementById('quiz-management').innerHTML = '<p style="color: red;">Error loading quizzes</p>';
        }
    }
    
    // Display quizzes
    function displayQuizzes(quizzes) {
        const container = document.getElementById('quiz-management');
        if (!quizzes || quizzes.length === 0) {
            container.innerHTML = '<p>No quizzes found</p>';
            return;
        }
        
        container.innerHTML = '<div class="quiz-grid"></div>';
        const grid = container.querySelector('.quiz-grid');
        
        quizzes.forEach(quiz => {
            const card = document.createElement('div');
            card.className = 'quiz-admin-card';
            card.innerHTML = `
                <div class="quiz-card-header">
                    <h3>${quiz.title}</h3>
                    <span class="quiz-badge ${quiz.isPublished ? 'published' : 'draft'}">
                        ${quiz.isPublished ? 'Published' : 'Draft'}
                    </span>
                </div>
                <p class="quiz-description">${quiz.description || 'No description'}</p>
                <div class="quiz-stats">
                    <span>Questions: ${quiz.questions ? quiz.questions.length : 0}</span>
                </div>
                <div class="quiz-actions">
                    <button class="btn-edit" onclick="adminPanel.editQuiz(${quiz.id})">Edit</button>
                    <button class="btn-questions" onclick="adminPanel.manageQuestions(${quiz.id}, '${quiz.title}')">Questions</button>
                    <button class="btn-toggle" onclick="adminPanel.togglePublish(${quiz.id}, ${quiz.isPublished})">
                        ${quiz.isPublished ? 'Unpublish' : 'Publish'}
                    </button>
                    <button class="btn-delete" onclick="adminPanel.deleteQuiz(${quiz.id})">Delete</button>
                </div>
            `;
            grid.appendChild(card);
        });
    }
    
    // Create new quiz
    async function createNewQuiz() {
        const title = prompt('Enter quiz title:');
        if (!title) return;
        
        const description = prompt('Enter quiz description (optional):');
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, description })
            });
            
            if (!response.ok) {
                throw new Error('Failed to create quiz');
            }
            
            alert('Quiz created successfully!');
            loadQuizList();
        } catch (error) {
            console.error('Error creating quiz:', error);
            alert('Error creating quiz: ' + error.message);
        }
    }
    
    // Edit quiz
    async function editQuiz(quizId) {
        const title = prompt('Enter new quiz title:');
        if (!title) return;
        
        const description = prompt('Enter new quiz description (optional):');
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes/${quizId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, description })
            });
            
            if (!response.ok) {
                throw new Error('Failed to update quiz');
            }
            
            alert('Quiz updated successfully!');
            loadQuizList();
        } catch (error) {
            console.error('Error updating quiz:', error);
            alert('Error updating quiz: ' + error.message);
        }
    }
    
    // Delete quiz
    async function deleteQuiz(quizId) {
        if (!confirm('Are you sure you want to delete this quiz?')) return;
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes/${quizId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error('Failed to delete quiz');
            }
            
            alert('Quiz deleted successfully!');
            loadQuizList();
        } catch (error) {
            console.error('Error deleting quiz:', error);
            alert('Error deleting quiz: ' + error.message);
        }
    }
    
    // Toggle publish status
    async function togglePublish(quizId, currentStatus) {
        const action = currentStatus ? 'unpublish' : 'publish';
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes/${quizId}/${action}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`Failed to ${action} quiz`);
            }
            
            alert(`Quiz ${action}ed successfully!`);
            loadQuizList();
        } catch (error) {
            console.error(`Error ${action}ing quiz:`, error);
            alert(`Error ${action}ing quiz: ` + error.message);
        }
    }
    
    // Manage questions for a quiz
    async function manageQuestions(quizId, quizTitle) {
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/questions/quiz/${quizId}`);
            if (!response.ok) {
                throw new Error('Failed to load questions');
            }
            const questions = await response.json();
            displayQuestionManager(quizId, quizTitle, questions);
        } catch (error) {
            console.error('Error loading questions:', error);
            alert('Error loading questions: ' + error.message);
        }
    }
    
    // Display question manager modal
    function displayQuestionManager(quizId, quizTitle, questions) {
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Manage Questions: ${quizTitle}</h2>
                    <button class="btn-close" onclick="this.closest('.modal-overlay').remove()">×</button>
                </div>
                <div class="modal-body">
                    <button class="btn-primary" onclick="adminPanel.addQuestion(${quizId})">Add New Question</button>
                    <div id="questions-list" class="questions-list">
                        ${questions.length === 0 ? '<p>No questions yet</p>' : ''}
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        
        if (questions.length > 0) {
            const list = modal.querySelector('#questions-list');
            questions.forEach((q, index) => {
                const questionDiv = document.createElement('div');
                questionDiv.className = 'question-item';
                questionDiv.innerHTML = `
                    <div class="question-header">
                        <strong>${index + 1}. ${q.text}</strong>
                        <span class="question-type">${q.qtype}</span>
                    </div>
                    ${q.choices && q.choices.length > 0 ? `
                        <div class="question-choices">
                            ${q.choices.map(c => `<div class="choice ${c.isCorrect ? 'correct' : ''}">${c.text}</div>`).join('')}
                        </div>
                    ` : ''}
                    <div class="question-actions">
                        <button class="btn-delete-sm" onclick="adminPanel.deleteQuestion(${q.id}, ${quizId})">Delete</button>
                    </div>
                `;
                list.appendChild(questionDiv);
            });
        }
    }
    
    // Add new question
    async function addQuestion(quizId) {
        const text = prompt('Enter question text:');
        if (!text) return;
        
        const qtype = prompt('Enter question type (SINGLE, MULTIPLE, SHORT_TEXT):');
        if (!qtype || !['SINGLE', 'MULTIPLE', 'SHORT_TEXT'].includes(qtype)) {
            alert('Invalid question type');
            return;
        }
        
        const auxText = prompt('Enter auxiliary text (optional, leave empty to skip):') || null;
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/questions`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quizId, qtype, text, auxText })
            });
            
            if (!response.ok) {
                throw new Error('Failed to create question');
            }
            
            alert('Question added successfully!');
            // Close modal and reload
            document.querySelector('.modal-overlay')?.remove();
            loadQuizList();
        } catch (error) {
            console.error('Error adding question:', error);
            alert('Error adding question: ' + error.message);
        }
    }
    
    // Delete question
    async function deleteQuestion(questionId, quizId) {
        if (!confirm('Are you sure you want to delete this question?')) return;
        
        try {
            const response = await window.auth.authFetch(`${apiBaseUrl}/questions/${questionId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error('Failed to delete question');
            }
            
            alert('Question deleted successfully!');
            // Close modal and reload
            document.querySelector('.modal-overlay')?.remove();
            loadQuizList();
        } catch (error) {
            console.error('Error deleting question:', error);
            alert('Error deleting question: ' + error.message);
        }
    }
    
    // Load statistics
    async function loadStatistics() {
        try {
            // For now, use quiz count only. User count requires admin endpoint
            // TODO: Create admin-specific statistics endpoint
            
            // Load quizzes count
            const quizzesResponse = await window.auth.authFetch(`${apiBaseUrl}/secure/admin/quizzes`);
            if (quizzesResponse.ok) {
                const quizzes = await quizzesResponse.json();
                document.getElementById('quiz-count').textContent = quizzes.length;
            }
            
            // Placeholders for future implementation
            document.getElementById('user-count').textContent = 'N/A';
            document.getElementById('attempt-count').textContent = 'N/A';
        } catch (error) {
            console.error('Error loading statistics:', error);
        }
    }
    
    // Export public functions
    return {
        init,
        createNewQuiz,
        editQuiz,
        deleteQuiz,
        togglePublish,
        manageQuestions,
        addQuestion,
        deleteQuestion,
        loadQuizList
    };
})();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    adminPanel.init();
});
