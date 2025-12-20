// main.js für alle Seiten (Login, Registrierung, Dashboard, Quiz...)

// --- Login ---
if(document.getElementById("loginForm")) {
    document.getElementById("loginForm").addEventListener("submit", async function(e){
        e.preventDefault();
        const data = Object.fromEntries(new FormData(this));
        let res = await fetch("/api/users/login", {
            method: "POST", headers: {"Content-Type":"application/json"},
            body: JSON.stringify(data)
        });
        let user = await res.json();
        if(user.id) {
            localStorage.setItem("radbrain_user", JSON.stringify(user));
            window.location.href = "dashboard.html";
        } else {
            alert(user.error || "Login fehlgeschlagen");
        }
    });
}

// --- Registrierung ---

if(document.getElementById("registerForm")) {
    document.getElementById("registerForm").addEventListener("submit", async function(e){
        e.preventDefault();
        const data = Object.fromEntries(new FormData(this));
        let res = await fetch("/api/users/register", {
            method: "POST", headers: {"Content-Type":"application/json"},
            body: JSON.stringify(data)
        });
        let user = await res.json();
        if(user.id) {
            alert("Registrierung erfolgreich!");
            window.location.href = "index.html";
        } else {
            alert(user.error || "Registrierung fehlgeschlagen");
        }
    });
}

// --- Logout ---
if(document.getElementById("logoutBtn") || document.getElementById("logoutBtnQuiz")) {
    document.getElementById("logoutBtn")?.addEventListener("click", function(e){
        e.preventDefault();
        localStorage.removeItem("radbrain_user");
        window.location.href = "index.html";
    });
    document.getElementById("logoutBtnQuiz")?.addEventListener("click", function(e){
        e.preventDefault();
        localStorage.removeItem("radbrain_user");
        window.location.href = "index.html";
    });
}

// --- Dashboard-Profil/Versuche ---
if(document.getElementById("profile")) {
    const user = JSON.parse(localStorage.getItem("radbrain_user") || "{}");
    fetch(`/api/users/${user.id}`).then(r=>r.json()).then(data=>{
        document.getElementById("profile").innerHTML = `
            <p>Name: <b>${data.fullname}</b></p>
            <p>Email: <b>${data.email}</b></p>
            <p>Status: ${data.is_active ? "Aktiv" : "Inaktiv"}</p>
        `
    });
    fetch(`/api/attempts/user/${user.id}`).then(r=>r.json()).then(list=>{
        if(!list.length){document.getElementById("attempts").innerHTML = "<i>Noch keine Versuche!</i>"; return;}
        document.getElementById("attempts").innerHTML = list.map(a=>{
            return `
                <div>
                    Quiz: ${a.quiz?.title || a.quiz?.id} | Score: ${a.score_pct}% | ${a.completed_at ? "Abgeschlossen" : "Offen"}
                </div>
            `;
        }).join("");
    });
}

// --- Quiz-Liste + Quiz-Taking ---
if(document.getElementById("quiz-list")) {
    const user = JSON.parse(localStorage.getItem("radbrain_user") || "{}");
    fetch("/api/quizzes/published")
        .then(r=>r.json())
        .then(quizzes=>{
            document.getElementById("quiz-list").innerHTML = quizzes.map(q=>{
                return `
                    <div class="quiz-card-large">
                        <div class="quiz-header">
                            <h3>${q.title}</h3>
                            <span class="quiz-badge">${q.questions?.length || "-" } Fragen</span>
                        </div>
                        <p>${q.description}</p>
                        <div class="quiz-info">
                            <span class="info-item">${q.isPublished?'Öffentlich':'Privat'}</span>
                        </div>
                        <button class="btn-quiz" onclick="startQuiz(${q.id}, '${q.title}')">Quiz starten</button>
                    </div>
                `;
            }).join('');
        });

    // Start Quiz Funktion global machen
    window.startQuiz = async function(quizId, quizTitle){
        document.getElementById("quiz-area").style.display = "block";
        document.getElementById("quiz-list").style.display = "none";
        document.getElementById("quiz-area").innerHTML = `<h2>${quizTitle}</h2><div id="quiz-form"></div>`;
        // Quiz anfangen (Backend)
        let attRes = await fetch("/api/attempts/start", {
            method:"POST", headers:{"Content-Type":"application/json"},
            body: JSON.stringify({quizId, userId:user.id})
        });
        let attempt = await attRes.json();
        let qsRes = await fetch(`/api/questions/quiz/${quizId}`);
        let questions = await qsRes.json();
        let html = `<form id="quizForm">` + questions.map((q,i)=>{
            // Render Choices/Options
            let options = (q.choices||[]).map(c=>`
                <label>
                    <input type="${q.qtype==='MULTIPLE'?'checkbox':'radio'}"
                        name="q${q.id}" value="${c.id}"> ${c.text}
                </label>
            `).join('');
            return `<div><b>${i+1}. ${q.text}</b><div>${options}</div></div>`;
        }).join('');
        html += `<button type="submit" class="btn-primary">Abschicken</button></form>`;
        document.getElementById("quiz-form").innerHTML = html;

        document.getElementById("quizForm").onsubmit = async function(e){
            e.preventDefault();
            let answers = {};
            questions.forEach(q => {
                if(q.qtype==='MULTIPLE')
                    answers[q.id]=Array.from(document.querySelectorAll(`input[name=q${q.id}]:checked`)).map(x=>x.value)
                else
                    answers[q.id]=document.querySelector(`input[name=q${q.id}]:checked`)?.value||null;
            });
            let scorePct = Math.round(Math.random()*100); // Dummy score
            await fetch(`/api/attempts/${attempt.id}/complete`, {
                method:"POST", headers:{"Content-Type":"application/json"},
                body: JSON.stringify({answersJson: JSON.stringify(answers), scorePct})
            });
            alert("Quiz abgeschlossen! (Dein Ergebnis wurde gespeichert)");
            window.location.href="dashboard.html";
        }
    }
}