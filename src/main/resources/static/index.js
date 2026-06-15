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

    // UI Elements - Voice
    const recordBtn = document.getElementById('record-btn');
    const timerDisplay = document.getElementById('recording-timer');
    const voiceStatus = document.getElementById('voice-status');
    const responseBox = document.getElementById('response-box');
    const responseText = document.getElementById('response-text');

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

    // Initial load
    fetchTransactions();

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
                body: formData
            });

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

    /* ==========================================================================
       CRUD Section
       ========================================================================== */
    async function fetchTransactions() {
        try {
            const response = await fetch(TRANSACTIONS_API);
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
            const response = await fetch(`${TRANSACTIONS_API}/${id}`, { method: 'DELETE' });
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
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bodyData)
            });

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
        const expenses = transactions.filter(t => t.tipo === 'DESPESA');
        
        // 1. Group expenses by category
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
                                color: '#9fa6bc',
                                font: { family: 'Outfit', size: 10 },
                                boxWidth: 8
                            }
                        },
                        title: {
                            display: true,
                            text: 'Despesas por Categoria',
                            color: '#f8f9fa',
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
                        grid: { color: 'rgba(255,255,255,0.03)' },
                        ticks: { color: '#9fa6bc', font: { family: 'Outfit', size: 10 } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#9fa6bc', font: { family: 'Outfit', size: 10 } }
                    }
                },
                plugins: {
                    legend: { display: false },
                    title: {
                        display: true,
                        text: 'Comparativo Geral',
                        color: '#f8f9fa',
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
