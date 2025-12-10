
// State
let token = localStorage.getItem('jwt_token');
let userRole = localStorage.getItem('user_role');
const API_BASE = 'http://localhost:8082';

// Init
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        showApp();
    } else {
        showLogin();
    }
});

// Navigation logic
function showLogin() {
    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('navbar').classList.add('hidden');
    document.querySelectorAll('.view-section').forEach(el => el.classList.add('hidden'));
}

function showApp() {
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('navbar').classList.remove('hidden');
    showSection('dashboard');
}

function showSection(sectionId) {
    document.querySelectorAll('.view-section').forEach(el => el.classList.add('hidden'));
    document.getElementById(`${sectionId}-screen`).classList.remove('hidden');

    // Update active nav
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelector(`[data-target="${sectionId}"]`)?.classList.add('active');

    // Load Data
    if (sectionId === 'affiliates') loadAffiliates();
    if (sectionId === 'credits') loadCredits();
}

// Event Listeners for Nav
document.querySelectorAll('.nav-link[data-target]').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        showSection(e.target.dataset.target);
    });
});

document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
    token = null;
    showLogin();
});

// LOGIN
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (res.ok) {
            const data = await res.json();
            token = data.token;
            localStorage.setItem('jwt_token', token);
            showApp();
        } else {
            showError('loginError', 'Credenciales incorrectas');
        }
    } catch (err) {
        showError('loginError', 'Error de conexión');
    }
});

// AFFILIATES
async function loadAffiliates() {
    try {
        const res = await fetchAuth(`${API_BASE}/api/afiliados`);
        if (!res.ok) throw new Error('Failed to load');
        const affiliates = await res.json();

        const tbody = document.getElementById('affiliatesTableBody');
        tbody.innerHTML = affiliates.map(af => `
            <tr>
                <td>${af.document}</td>
                <td>${af.name}</td>
                <td>$${af.salary.toLocaleString()}</td>
                <td>${af.affiliationDate}</td>
                <td>
                    <span class="badge ${af.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">
                        ${af.status}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="editAffiliate('${af.document}')">Editar</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
    }
}

document.getElementById('affiliateForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        document: document.getElementById('afDocument').value,
        name: document.getElementById('afName').value,
        salary: parseFloat(document.getElementById('afSalary').value),
        affiliationDate: document.getElementById('afDate').value,
        status: 'ACTIVE'
    };

    try {
        const res = await fetchAuth(`${API_BASE}/api/afiliados`, {
            method: 'POST',
            body: JSON.stringify(data)
        });

        if (res.ok) {
            closeModal('affiliateModal');
            loadAffiliates();
            e.target.reset();
        } else {
            alert('Error al guardar afiliado (verifique permisos o duplicados)');
        }
    } catch (err) {
        alert('Error de red');
    }
});

// CREDITS
async function loadCredits() {
    try {
        const res = await fetchAuth(`${API_BASE}/api/solicitudes`);
        if (!res.ok) throw new Error('Failed to load');
        const credits = await res.json();

        const tbody = document.getElementById('creditsTableBody');
        tbody.innerHTML = credits.map(cr => `
            <tr>
                <td>${cr.id}</td>
                <td>$${cr.requestedAmount.toLocaleString()}</td>
                <td>${cr.termMonths} meses</td>
                <td>
                    <span class="badge ${getStatusBadge(cr.status)}">
                        ${cr.status}
                    </span>
                </td>
                <td>
                   ${cr.riskEvaluation ?
                `<span class="badge ${cr.riskEvaluation.riskLevel === 'BAJO' ? 'badge-success' : 'badge-danger'}">
                          ${cr.riskEvaluation.riskLevel} (${cr.riskEvaluation.score})
                        </span>`
                : '-'}
                </td>
                <td>${new Date(cr.applicationDate).toLocaleDateString()}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
    }
}

document.getElementById('creditForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        affiliateDocument: document.getElementById('crDocument').value,
        amount: parseFloat(document.getElementById('crAmount').value),
        term: parseInt(document.getElementById('crTerm').value)
    };

    try {
        const res = await fetchAuth(`${API_BASE}/api/solicitudes`, {
            method: 'POST',
            body: JSON.stringify(data)
        });

        if (res.ok) {
            closeModal('creditModal');
            loadCredits();
            showSection('credits'); // Redirect to list
            e.target.reset();
            alert('Solicitud creada existosamente');
        } else {
            let errorMsg = 'Solicitud rechazada';
            try {
                const err = await res.json();
                errorMsg = err.detail || err.message || errorMsg;
            } catch (jsonErr) {
                if (res.status === 403) errorMsg = 'Acceso denegado: No tienes permisos (Rol incorrecto)';
                else errorMsg = `Error del servidor (${res.status})`;
            }
            alert('Error: ' + errorMsg);
        }
    } catch (err) {
        alert('Error conectando al servidor (Red/CORS)');
    }
});


// UTILS
async function fetchAuth(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };
    return fetch(url, { ...options, headers });
}

function getStatusBadge(status) {
    switch (status) {
        case 'APPROVED': return 'badge-success';
        case 'REJECTED': return 'badge-danger';
        case 'PENDING': return 'badge-warning';
        default: return 'badge-neutral';
    }
}

// Modals
window.openModal = (id) => {
    document.getElementById(id).classList.add('show');
}

window.closeModal = (id) => {
    document.getElementById(id).classList.remove('show');
}

window.showError = (id, msg) => {
    const el = document.getElementById(id);
    el.innerText = msg;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 3000);
}

// Edit Affiliate - Fetch and populate modal
window.editAffiliate = async (doc) => {
    try {
        const res = await fetchAuth(`${API_BASE}/api/afiliados/${doc}`);
        if (!res.ok) throw new Error('Affiliate not found');
        const affiliate = await res.json();

        // Populate form
        document.getElementById('editAfDocument').value = affiliate.document;
        document.getElementById('editAfDocumentDisplay').value = affiliate.document;
        document.getElementById('editAfName').value = affiliate.name;
        document.getElementById('editAfSalary').value = affiliate.salary;
        document.getElementById('editAfStatus').value = affiliate.status;

        openModal('editAffiliateModal');
    } catch (err) {
        alert('Error al cargar afiliado: ' + err.message);
    }
}

// Edit Affiliate Form Submit
document.getElementById('editAffiliateForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const doc = document.getElementById('editAfDocument').value;
    const data = {
        document: doc,
        name: document.getElementById('editAfName').value,
        salary: parseFloat(document.getElementById('editAfSalary').value),
        status: document.getElementById('editAfStatus').value
    };

    try {
        const res = await fetchAuth(`${API_BASE}/api/afiliados/${doc}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });

        if (res.ok) {
            closeModal('editAffiliateModal');
            loadAffiliates();
            alert('Afiliado actualizado correctamente');
        } else {
            const err = await res.json();
            alert('Error: ' + (err.detail || 'No se pudo actualizar'));
        }
    } catch (err) {
        alert('Error de conexión');
    }
});

