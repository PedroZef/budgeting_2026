/* ==========================================================================
   Finanças IA - Frontend Logic
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // API Endpoints
    const ASSISTANT_API = '/api/assistant/voice';
    const TRANSACTIONS_API = '/api/transactions';

    // State Variables
    let mediaRecorder = null;
    let audioChunks = [];
    let isRecording = false;
    let recordingTimerInterval = null;
    let recordingSeconds = 0;
    
    let transactions = [];
    let categoryChartInstance = null;
    let trendChartInstance = null;

    // UI Elements - Voice & Text Assistant
    const recordBtn = document.getElementById('record-btn');
    const timerDisplay = document.getElementById('recording-timer');
    const voiceStatus = document.getElementById('voice-status');
    const responseBox = document.getElementById('response-box');
    const responseText = document.getElementById('response-text');
    const queryForm = document.getElementById('query-form');
    const queryInput = document.getElementById('query-input');

    // UI Elements - Table & Metrics
    const transactionList = document.getElementById('transaction-list');
    const totalIncomeDisplay = document.getElementById('total-income');
    const totalExpenseDisplay = document.getElementById('total-expense');
    const netBalanceDisplay = document.getElementById('net-balance');

    // UI Elements - Modal & Form
    const transactionModal = document.getElementById('transaction-modal');
    const newTransactionBtn = document.getElementById('new-transaction-btn');
    const modalCloseBtn = document.getElementById('modal-close-btn');
    const formCancelBtn = document.getElementById('form-cancel');
    const transactionForm = document.getElementById('transaction-form');
    const modalTitle = document.getElementById('modal-title');
    
    const formId = document.getElementById('form-id');
    const formValor = document.getElementById('form-valor');
    const formCategoria = document.getElementById('form-categoria');
    const formTipo = document.getElementById('form-tipo');
    const formUsuario = document.getElementById('form-usuario');

    // Helper function to inject Authorization headers
    function getAuthHeaders(headers = {}) {
        const token = localStorage.getItem('token');
        if (token) {
            return {
                ...headers,
                'Authorization': `Bearer ${token}`
            };
        }
        return headers;
    }

    // UI Elements - Theme & Authentication
    const themeToggleBtn = document.getElementById('theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    const loginOverlay = document.getElementById('login-overlay');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginUsernameInput = document.getElementById('login-username');
    const loginPasswordInput = document.getElementById('login-password');
    const registerUsernameInput = document.getElementById('register-username');
    const registerPasswordInput = document.getElementById('register-password');
    const authErrorMsg = document.getElementById('auth-error-msg');
    const switchToRegisterBtn = document.getElementById('switch-to-register');
    const switchToLoginBtn = document.getElementById('switch-to-login');
    const logoutBtn = document.getElementById('logout-btn');
    const headerUser = document.getElementById('header-user');

    // Theme toggle initialization and event listener
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
        themeIcon.classList.remove('fa-sun');
        themeIcon.classList.add('fa-moon');
    }

    themeToggleBtn.addEventListener('click', () => {
        document.body.classList.toggle('light-theme');
        const isLight = document.body.classList.contains('light-theme');
        if (isLight) {
            themeIcon.classList.remove('fa-sun');
            themeIcon.classList.add('fa-moon');
            localStorage.setItem('theme', 'light');
        } else {
            themeIcon.classList.remove('fa-moon');
            themeIcon.classList.add('fa-sun');
            localStorage.setItem('theme', 'dark');
        }
        updateCharts(); // Re-render charts with correct text colors
    });

    // Switch between Login and Register
    switchToRegisterBtn.addEventListener('click', () => {
        loginForm.classList.add('hidden');
        registerForm.classList.remove('hidden');
        authErrorMsg.innerText = '';
    });

    switchToLoginBtn.addEventListener('click', () => {
        registerForm.classList.add('hidden');
        loginForm.classList.remove('hidden');
        authErrorMsg.innerText = '';
    });

    function checkAuthState() {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        if (token && username) {
            loginOverlay.classList.add('hidden');
            logoutBtn.classList.remove('hidden');
            headerUser.innerText = username;
            fetchTransactions();
        } else {
            loginOverlay.classList.remove('hidden');
            logoutBtn.classList.add('hidden');
            headerUser.innerText = 'Sistema Offline';
        }
    }

    function handleLogout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        checkAuthState();
        // Clear UI
        transactions = [];
        renderTransactions();
        updateCharts();
    }

    // Login Form Submit
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        authErrorMsg.style.color = '#ff4e7e';
        authErrorMsg.innerText = '';
        const username = loginUsernameInput.value.trim();
        const password = loginPasswordInput.value;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('Usuário ou senha inválidos.');
            }

            const data = await response.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            
            // Clean inputs
            loginUsernameInput.value = '';
            loginPasswordInput.value = '';
            
            checkAuthState();
        } catch (err) {
            authErrorMsg.innerText = err.message;
        }
    });

    // Register Form Submit
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        authErrorMsg.style.color = '#ff4e7e';
        authErrorMsg.innerText = '';
        const username = registerUsernameInput.value.trim();
        const password = registerPasswordInput.value;

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Erro ao registrar usuário.');
            }

            authErrorMsg.style.color = '#05e695';
            authErrorMsg.innerText = 'Usuário registrado com sucesso! Faça login.';
            
            registerUsernameInput.value = '';
            registerPasswordInput.value = '';
            
            setTimeout(() => {
                authErrorMsg.style.color = '#ff4e7e';
                registerForm.classList.add('hidden');
                loginForm.classList.remove('hidden');
                authErrorMsg.innerText = '';
            }, 1500);

        } catch (err) {
            authErrorMsg.innerText = err.message;
        }
    });

    // Logout Button
    logoutBtn.addEventListener('click', handleLogout);

    // Initial load
    checkAuthState();

    /* ==========================================================================
       Voice Recording Section
       ========================================================================== */
    recordBtn.addEventListener('click', toggleRecording);

    async function toggleRecording() {
        if (!isRecording) {
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                startRecording(stream);
            } catch (err) {
                console.error('Erro ao acessar microfone:', err);
                voiceStatus.innerText = 'Erro: Microfone inacessível.';
            }
        } else {
            stopRecording();
        }
    }

    function startRecording(stream) {
        audioChunks = [];
        // Detect standard mime types
        let mimeType = 'audio/webm';
        if (MediaRecorder.isTypeSupported('audio/ogg')) mimeType = 'audio/ogg';
        else if (MediaRecorder.isTypeSupported('audio/mp4')) mimeType = 'audio/mp4';

        mediaRecorder = new MediaRecorder(stream, { mimeType });
        
        mediaRecorder.ondataavailable = event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = () => {
            const extension = mimeType.split('/')[1].split(';')[0];
            const audioBlob = new Blob(audioChunks, { type: mimeType });
            sendAudioToAI(audioBlob, extension);
            
            // Stop all tracks to release microphone
            stream.getTracks().forEach(track => track.stop());
        };

        mediaRecorder.start();
        isRecording = true;
        recordBtn.classList.add('recording');
        voiceStatus.innerText = 'Ouvindo... Fale agora!';
        
        // Timer
        recordingSeconds = 0;
        timerDisplay.innerText = '00:00';
        recordingTimerInterval = setInterval(() => {
            recordingSeconds++;
            const mins = String(Math.floor(recordingSeconds / 60)).padStart(2, '0');
            const secs = String(recordingSeconds % 60).padStart(2, '0');
            timerDisplay.innerText = `${mins}:${secs}`;
        }, 1000);
    }

    function stopRecording() {
        if (mediaRecorder && isRecording) {
            mediaRecorder.stop();
            isRecording = false;
            recordBtn.classList.remove('recording');
            clearInterval(recordingTimerInterval);
            voiceStatus.innerText = 'Processando áudio com a IA...';
        }
    }

    async function sendAudioToAI(blob, extension) {
        const formData = new FormData();
        const filename = `voice_command.${extension}`;
        formData.append('audio', blob, filename);

        try {
            const response = await fetch(ASSISTANT_API, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: formData
            });

            if (response.status === 401 || response.status === 403) {
                handleLogout();
                return;
            }

            if (!response.ok) {
                throw new Error('Falha no processamento do servidor.');
            }

            const data = await response.text();
            
            // Display response
            responseText.innerText = data;
            responseBox.classList.remove('hidden');
            voiceStatus.innerText = 'Comando processado com sucesso!';
            
            // Refresh database
            fetchTransactions();
        } catch (err) {
            console.error('Erro ao enviar áudio:', err);
            voiceStatus.innerText = 'Erro ao processar áudio.';
            responseText.innerText = 'Erro: ' + err.message;
            responseBox.classList.remove('hidden');
        }
    }

    // Handle text query submission
    queryForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const queryText = queryInput.value.trim();
        if (!queryText) return;

        voiceStatus.innerText = 'Processando consulta com a IA...';
        responseText.innerText = '';
        responseBox.classList.add('hidden');

        try {
            const response = await fetch('/api/assistant/text', {
                method: 'POST',
                headers: getAuthHeaders({
                    'Content-Type': 'text/plain'
                }),
                body: queryText
            });

            if (response.status === 401 || response.status === 403) {
                handleLogout();
                return;
            }

            if (!response.ok) {
                throw new Error('Falha no processamento do servidor.');
            }

            const data = await response.text();
            
            // Display response
            responseText.innerText = data;
            responseBox.classList.remove('hidden');
            voiceStatus.innerText = 'Consulta processada com sucesso!';
            queryInput.value = ''; // clear input
            
            // Refresh database in case the query was actually a command that changed state
            fetchTransactions();
        } catch (err) {
            console.error('Erro ao enviar consulta:', err);
            voiceStatus.innerText = 'Erro ao processar consulta.';
            responseText.innerText = 'Erro: ' + err.message;
            responseBox.classList.remove('hidden');
        }
    });

    /* ==========================================================================
       CRUD Section
       ========================================================================== */
    async function fetchTransactions() {
        try {
            const response = await fetch(TRANSACTIONS_API, {
                headers: getAuthHeaders()
            });
            if (response.status === 401 || response.status === 403) {
                handleLogout();
                return;
            }
            if (!response.ok) throw new Error('Não foi possível carregar as transações.');
            transactions = await response.json();
            renderTransactions();
            updateCharts();
        } catch (err) {
            console.error('Erro ao buscar transações:', err);
            transactionList.innerHTML = `<tr><td colspan="6" class="empty-state">Erro ao carregar dados: ${err.message}</td></tr>`;
        }
    }

    function renderTransactions() {
        if (transactions.length === 0) {
            transactionList.innerHTML = `<tr><td colspan="6" class="empty-state">Nenhuma transação cadastrada. Use o comando de voz!</td></tr>`;
            updateMetrics(0, 0, 0);
            return;
        }

        let totalIncome = 0;
        let totalExpense = 0;

        transactionList.innerHTML = '';
        
        transactions.forEach(t => {
            const isReceita = t.tipo === 'RECEITA';
            const val = parseFloat(t.valor);
            
            if (isReceita) totalIncome += val;
            else totalExpense += val;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${formatDate(t.data)}</td>
                <td>${t.usuario || 'pedro'}</td>
                <td><i class="${getCategoryIcon(t.categoria)}"></i> ${t.categoria}</td>
                <td><span class="badge-tipo ${t.tipo.toLowerCase()}">${t.tipo}</span></td>
                <td class="valor-col ${t.tipo.toLowerCase()}">${isReceita ? '+' : '-'} R$ ${val.toFixed(2)}</td>
                <td class="action-buttons">
                    <button class="action-btn edit-btn" data-id="${t.id}"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="action-btn delete-btn" data-id="${t.id}"><i class="fa-solid fa-trash"></i></button>
                </td>
            `;

            // Bind events
            tr.querySelector('.edit-btn').addEventListener('click', () => openEditModal(t));
            tr.querySelector('.delete-btn').addEventListener('click', () => deleteTransaction(t.id));

            transactionList.appendChild(tr);
        });

        const netBalance = totalIncome - totalExpense;
        updateMetrics(totalIncome, totalExpense, netBalance);
    }

    function updateMetrics(income, expense, balance) {
        totalIncomeDisplay.innerText = `R$ ${income.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        totalExpenseDisplay.innerText = `R$ ${expense.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        netBalanceDisplay.innerText = `${balance >= 0 ? '' : '-'} R$ ${Math.abs(balance).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }

    async function deleteTransaction(id) {
        if (!confirm('Deseja realmente remover esta transação?')) return;
        try {
            const response = await fetch(`${TRANSACTIONS_API}/${id}`, { 
                method: 'DELETE',
                headers: getAuthHeaders()
            });
            if (response.status === 401 || response.status === 403) {
                handleLogout();
                return;
            }
            if (!response.ok) throw new Error('Erro ao deletar transação.');
            fetchTransactions();
        } catch (err) {
            alert('Falha ao deletar: ' + err.message);
        }
    }

    /* ==========================================================================
       Modal & Form Handling
       ========================================================================== */
    newTransactionBtn.addEventListener('click', () => openCreateModal());
    modalCloseBtn.addEventListener('click', closeModal);
    formCancelBtn.addEventListener('click', closeModal);

    function openCreateModal() {
        modalTitle.innerHTML = '<i class="fa-solid fa-plus"></i> Nova Transação';
        formId.value = '';
        formValor.value = '';
        formCategoria.value = '';
        formTipo.value = 'DESPESA';
        formUsuario.value = '';
        transactionModal.classList.remove('hidden');
    }

    function openEditModal(t) {
        modalTitle.innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Editar Transação';
        formId.value = t.id;
        formValor.value = t.valor;
        formCategoria.value = t.categoria;
        formTipo.value = t.tipo;
        formUsuario.value = t.usuario || '';
        transactionModal.classList.remove('hidden');
    }

    function closeModal() {
        transactionModal.classList.add('hidden');
    }

    transactionForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const id = formId.value;
        const bodyData = {
            valor: parseFloat(formValor.value),
            categoria: formCategoria.value.trim(),
            tipo: formTipo.value,
            usuario: formUsuario.value.trim() || null
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${TRANSACTIONS_API}/${id}` : TRANSACTIONS_API;

        try {
            const response = await fetch(url, {
                method: method,
                headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(bodyData)
            });

            if (response.status === 401 || response.status === 403) {
                handleLogout();
                return;
            }

            if (!response.ok) throw new Error('Erro ao salvar transação.');
            
            closeModal();
            fetchTransactions();
        } catch (err) {
            alert('Falha ao salvar transação: ' + err.message);
        }
    });

    /* ==========================================================================
       Charts Section (Chart.js)
       ========================================================================== */
    function updateCharts() {
        const isLightTheme = document.body.classList.contains('light-theme');
        const textColorPrimary = isLightTheme ? '#0f172a' : '#f8f9fa';
        const textColorMuted = isLightTheme ? '#64748b' : '#9fa6bc';
        const gridColor = isLightTheme ? 'rgba(0,0,0,0.03)' : 'rgba(255,255,255,0.03)';

        const expenses = transactions.filter(t => t.tipo === 'DESPESA');
        const categories = {};
        expenses.forEach(e => {
            categories[e.categoria] = (categories[e.categoria] || 0) + parseFloat(e.valor);
        });

        const catLabels = Object.keys(categories);
        const catValues = Object.values(categories);

        // Render Category Doughnut
        if (categoryChartInstance) categoryChartInstance.destroy();
        
        const ctxCategory = document.getElementById('categoryChart').getContext('2d');
        if (catLabels.length === 0) {
            // Draw dummy chart if no data
            categoryChartInstance = new Chart(ctxCategory, {
                type: 'doughnut',
                data: {
                    labels: ['Sem dados'],
                    datasets: [{
                        data: [1],
                        backgroundColor: ['rgba(255,255,255,0.05)'],
                        borderWidth: 0
                    }]
                },
                options: {
                    cutout: '75%',
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } }
                }
            });
        } else {
            categoryChartInstance = new Chart(ctxCategory, {
                type: 'doughnut',
                data: {
                    labels: catLabels,
                    datasets: [{
                        data: catValues,
                        backgroundColor: [
                            '#a15bf9', '#f75b95', '#00bdf5', '#05e695', '#ff7b47', '#ffdb58', '#4970ff'
                        ],
                        hoverOffset: 4,
                        borderWidth: 0
                    }]
                },
                options: {
                    cutout: '70%',
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: textColorMuted,
                                font: { family: 'Outfit', size: 10 },
                                boxWidth: 8
                            }
                        },
                        title: {
                            display: true,
                            text: 'Despesas por Categoria',
                            color: textColorPrimary,
                            font: { family: 'Outfit', size: 12, weight: '600' }
                        }
                    }
                }
            });
        }

        // 2. Trend compare (Income vs Expense)
        let totalIncomeSum = 0;
        let totalExpenseSum = 0;
        transactions.forEach(t => {
            const val = parseFloat(t.valor);
            if (t.tipo === 'RECEITA') totalIncomeSum += val;
            else totalExpenseSum += val;
        });

        if (trendChartInstance) trendChartInstance.destroy();
        const ctxTrend = document.getElementById('trendChart').getContext('2d');
        
        trendChartInstance = new Chart(ctxTrend, {
            type: 'bar',
            data: {
                labels: ['Receitas', 'Despesas'],
                datasets: [{
                    data: [totalIncomeSum, totalExpenseSum],
                    backgroundColor: ['#05e695', '#ff4e7e'],
                    borderRadius: 8,
                    borderWidth: 0,
                    barPercentage: 0.5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        grid: { color: gridColor },
                        ticks: { color: textColorMuted, font: { family: 'Outfit', size: 10 } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: textColorMuted, font: { family: 'Outfit', size: 10 } }
                    }
                },
                plugins: {
                    legend: { display: false },
                    title: {
                        display: true,
                        text: 'Comparativo Geral',
                        color: textColorPrimary,
                        font: { family: 'Outfit', size: 12, weight: '600' }
                    }
                }
            }
        });
    }

    /* ==========================================================================
       Helper Utilities
       ========================================================================== */
    function formatDate(dateString) {
        if (!dateString) return '';
        const d = new Date(dateString);
        return d.toLocaleDateString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function getCategoryIcon(cat) {
        if (!cat) return 'fa-solid fa-tag';
        const clean = cat.toLowerCase();
        if (clean.includes('aliment') || clean.includes('comida') || clean.includes('mercado') || clean.includes('restaurante')) {
            return 'fa-solid fa-utensils';
        }
        if (clean.includes('salario') || clean.includes('renda') || clean.includes('recebi') || clean.includes('ganho')) {
            return 'fa-solid fa-hand-holding-dollar';
        }
        if (clean.includes('lazer') || clean.includes('divers') || clean.includes('cinema') || clean.includes('show')) {
            return 'fa-solid fa-gamepad';
        }
        if (clean.includes('transport') || clean.includes('combustivel') || clean.includes('gasolina') || clean.includes('uber') || clean.includes('onibus')) {
            return 'fa-solid fa-car';
        }
        if (clean.includes('saud') || clean.includes('remedio') || clean.includes('farmacia') || clean.includes('medico')) {
            return 'fa-solid fa-heart-pulse';
        }
        if (clean.includes('casa') || clean.includes('aluguel') || clean.includes('agua') || clean.includes('luz') || clean.includes('energia')) {
            return 'fa-solid fa-house-user';
        }
        return 'fa-solid fa-tag';
    }
});
