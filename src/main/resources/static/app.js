document.addEventListener('DOMContentLoaded', async () => {
    const API_URL = '/api/transactions';
    const AUTH_URL = '/api/auth/login';
    const ANALYTICS_URL = '/api/analytics/monthly-balance';
    const mainHeader = document.getElementById('main-header');
    mainHeader.classList.add('hide-header');

    // --- SECCIONES ---
    const loginSection = document.getElementById('login-section');
    const dashboard = document.getElementById('dashboard');

    // --- LOGIN ---

    const loginForm = document.getElementById('login-form');
    loginForm.addEventListener('submit', async e => {
        e.preventDefault();
        const usernameOrEmail = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        try {
            const res = await fetch(AUTH_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usernameOrEmail, password })
            });
            if (!res.ok) throw new Error('Usuario o contraseña incorrectos');
            const data = await res.json();
            localStorage.setItem('token', data.token);
            loginSection.style.display = 'none';
            dashboard.style.display = 'block';
            mainHeader.classList.remove('hide-header');
            await loadAllTransactions();
            await loadTransactions();
            await renderMonthlyBalanceChart();
        } catch (err) {
            Swal.fire({
              icon: 'error',
              title: 'Oops...',
              text: err.message
            });

        }
    });
    const showRegisterBtn = document.getElementById('show-register');
    const showRegisterP = document.getElementById('show-register-p');
    const registerForm = document.getElementById('register-form');
    document.getElementById('show-register').addEventListener('click', () => {
        registerForm.style.display = 'block';
        loginForm.style.display = 'none';
        showRegisterBtn.style.display = 'none';
        showRegisterP.style.display = 'none';
    });
    document.getElementById('cancel-register').addEventListener('click', () => {
        registerForm.style.display = 'none';
        loginForm.style.display = 'block';
        showRegisterBtn.style.display = 'inline-block';
        showRegisterP.style.display = 'block';
    });

    // Submit del registro
    registerForm.addEventListener('submit', async e => {
        e.preventDefault();
        const username = document.getElementById('register-username').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;

        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });
            if (!res.ok) throw new Error('Error al registrarse');
            Swal.fire({
              icon: 'success',
              title: '¡Éxito!',
              text: 'Registro exitoso. Ahora inicia sesión.'
            });
            registerForm.reset();
            registerForm.style.display = 'none';
            loginForm.style.display = 'block';
        } catch (err) {
            Swal.fire({
              icon: 'error',
              title: 'Oops...',
              text: err.message
            });
        }
    });

    // --- LOGOUT ---
    document.getElementById('logout-btn').addEventListener('click', () => {
        localStorage.removeItem('token');
        location.reload();
    });
    let currentPage = 0;
    const pageSize = 10;
    let totalPages = 1;

    // --- VARIABLES CRUD ---
    const form = document.getElementById('form-transaction');
    const tableBody = document.getElementById('table-body');
    const cancelEditBtn = document.getElementById('cancel-edit');

    let transactions = [];
    let allTransactions = [];
    let editId = null;

    // --- FILTROS ---
    const filterFrom = document.getElementById('filter-from');
    const filterTo = document.getElementById('filter-to');
    const filterCategory = document.getElementById('filter-category');
    const filterType = document.getElementById('filter-type');
    const filterMin = document.getElementById('filter-min');
    const filterMax = document.getElementById('filter-max');

    // --- GRÁFICOS ---
    const availableColors = [
        '#C8102E', '#FF5733', '#33C8FF', '#FFC300', '#8E44AD',
        '#27AE60', '#FF33A8', '#3498DB', '#F39C12', '#1ABC9C'
    ];
    // Mapa para mantener color por categoría
    const categoryColors = {};
    let colorIndex = 0;

    // Función para obtener color de categoría
    function getCategoryColor(category) {
        if (!categoryColors[category]) {
            // Asigna siguiente color disponible
            categoryColors[category] = availableColors[colorIndex % availableColors.length];
            colorIndex++;
        }
        return categoryColors[category];
    }
    let barChart, pieChart, lineChart;
    async function loadAllTransactions() {
        allTransactions = [];
        let page = 0;
        let totalPages = 1;

        while (page < totalPages) {
            const query = `?page=${page}&size=50`; // 50 o lo que tu backend permita
            const res = await fetch(`${API_URL}${query}`, {
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
            });
            if (!res.ok) break;

            const data = await res.json();
            allTransactions.push(...(data.content || []));
            totalPages = data.totalPages || 1;
            page++;
        }
    }

    // ======== FUNCIONES ========
    async function loadTransactions(page = 0) {
        currentPage = page;
        let query = `?page=${page}&size=${pageSize}`;

        // filtros aplicados solo para la tabla
        if (filterFrom.value) query += `&from=${encodeURIComponent(filterFrom.value)}`;
        if (filterTo.value) query += `&to=${encodeURIComponent(filterTo.value)}`;
        if (filterCategory.value) query += `&category=${encodeURIComponent(filterCategory.value)}`;
        if (filterType.value) query += `&type=${encodeURIComponent(filterType.value)}`;
        if (filterMin.value) query += `&minAmount=${encodeURIComponent(filterMin.value)}`;
        if (filterMax.value) query += `&maxAmount=${encodeURIComponent(filterMax.value)}`;

        const res = await fetch(`${API_URL}${query}`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) {
            Swal.fire({
                icon: 'error',
                title: 'Oops...',
                text: 'Error al cargar transacciones'
            });
            return;
        }

        const data = await res.json();
        transactions = data.content || [];
        totalPages = data.totalPages || 1;

        renderTable();
        renderCharts(); // ahora usa allTransactions
        renderAnalyticsPanel();
        renderPaginationControls();
    }

    function renderPaginationControls() {
        document.getElementById('page-info').textContent = `Página ${currentPage + 1} de ${totalPages}`;
        document.getElementById('prev-page').disabled = currentPage === 0;
        document.getElementById('next-page').disabled = currentPage >= totalPages - 1;
    }

    document.getElementById('prev-page').addEventListener('click', () => {
        if (currentPage > 0) loadTransactions(currentPage - 1);
    });

    document.getElementById('next-page').addEventListener('click', () => {
        if (currentPage < totalPages - 1) loadTransactions(currentPage + 1);
    });
    function formatCurrency(amount) {
        return '$' + amount.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
    }


    async function loadCategorySummary() {
        const res = await fetch('/api/analytics/category-summary', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) return [];
        const data = await res.json();
        return data.categories || [];
    }

    function renderTable() {
        tableBody.innerHTML = '';
        transactions.forEach(t => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${t.date}</td>
        <td>${formatCurrency(t.amount)}</td>
        <td>${t.category}</td>
        <td>${t.type}</td>
        <td>${t.description}</td>
        <td>
          <button onclick="editTransaction(${t.id})">Editar</button>
          <button onclick="deleteTransaction(${t.id})">Eliminar</button>
        </td>
      `;
            tableBody.appendChild(tr);
        });
    }

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const t = {
            date: document.getElementById('date').value,
            amount: parseFloat(document.getElementById('amount').value),
            category: document.getElementById('category').value,
            type: document.getElementById('type').value,
            description: document.getElementById('description').value
        };
        const method = editId ? 'PUT' : 'POST';
        const url = editId ? `${API_URL}/${editId}` : API_URL;

        const res = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify(t)
        });
        if (!res.ok) {
            Swal.fire({
                icon: 'error',
                title: 'Oops...',
                text: 'Error al guardar la transaccion'
            });
            return;
        }

        editId = null;
        form.reset();
        await loadTransactions();
    });

    window.editTransaction = (id) => {
        const t = transactions.find(x => x.id === id);
        editId = id;
        document.getElementById('date').value = t.date;
        document.getElementById('amount').value = t.amount;
        document.getElementById('category').value = t.category;
        document.getElementById('type').value = t.type;
        document.getElementById('description').value = t.description;
    }

    cancelEditBtn.addEventListener('click', () => { editId = null; form.reset(); });

    window.deleteTransaction = async (id) => {
        const result = await Swal.fire({
          title: '¿Seguro que deseas eliminar esta transacción?',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#d33',
          cancelButtonColor: '#3085d6',
          confirmButtonText: 'Sí, eliminar',
          cancelButtonText: 'Cancelar'
        });

        if (!result.isConfirmed) return;

        const res = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Error al eliminar la transacción');

        Swal.fire({
            icon: 'success',
            title: '¡Eliminado!',
            text: 'La transacción ha sido eliminada.'
        });
        const index = allTransactions.findIndex(t => t.id === id);
        if (index >= 0) allTransactions.splice(index, 1);
        await loadTransactions();
    }

    document.getElementById('apply-filters').addEventListener('click', () => loadTransactions());
    document.getElementById('reset-filters').addEventListener('click', () => {
        filterFrom.value = '';
        filterTo.value = '';
        filterCategory.value = '';
        filterType.value = '';
        filterMin.value = '';
        filterMax.value = '';
        loadTransactions();
    });

    // ===== GRÁFICOS =====
    async function renderMonthlyBalanceChart() {
        const res = await fetch(ANALYTICS_URL, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) return;
        const data = await res.json();

        const labels = data.map(d => d.yearMonth);
        const income = data.map(d => d.income);
        const expense = data.map(d => d.expense);

        const ctx = document.getElementById('line-chart').getContext('2d');
        if (lineChart) lineChart.destroy();

        lineChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    { label: 'Ingresos', data: income, borderColor: 'green', fill: false },
                    { label: 'Gastos', data: expense, borderColor: 'red', fill: false }
                ]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    async function renderCharts() {
    // Agrupar transacciones locales por categoría y tipo
    const categoryMap = {};
    allTransactions.forEach(t => {
        if (!categoryMap[t.category]) categoryMap[t.category] = { INCOME: 0, EXPENSE: 0 };
        categoryMap[t.category][t.type] += t.amount;
    });

    const labels = Object.keys(categoryMap);
    const incomeValues = labels.map(c => categoryMap[c].INCOME);
    const expenseValues = labels.map(c => categoryMap[c].EXPENSE);

    // --- Gráfico de barras: ingresos y gastos por categoría ---
    const barCtx = document.getElementById('bar-chart').getContext('2d');
    if (barChart) barChart.destroy();
    barChart = new Chart(barCtx, {
        type: 'bar',
        data: {
            labels,
            datasets: [
                { label: 'Ingresos', data: incomeValues, backgroundColor: 'rgba(54, 162, 235, 0.6)' },
                { label: 'Gastos', data: expenseValues, backgroundColor: 'rgba(255, 99, 132, 0.6)' }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true }
            }
        }
    });

    // --- Gráfico de pastel: distribución porcentual de gastos ---
    const pieLabels = labels.filter((_, i) => expenseValues[i] > 0);
    const pieValues = expenseValues.filter(v => v > 0);
    const pieColors = pieLabels.map(c => getCategoryColor(c)); // colores consistentes

    const pieCtx = document.getElementById('pie-chart').getContext('2d');
    if (pieChart) pieChart.destroy();
    pieChart = new Chart(pieCtx, {
        type: 'pie',
        data: {
            labels: pieLabels,
            datasets: [{
                label: 'Distribución porcentual de gastos',
                data: pieValues,
                backgroundColor: pieColors
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });

    await renderMonthlyBalanceChart();
}



    // ===== INDICADORES CLAVE =====
    async function renderAnalyticsPanel() {
        const categories = await loadCategorySummary();
        const totalExpenses = categories.reduce((sum, c) => sum + c.total, 0);
        const avgExpense = (totalExpenses / categories.length).toFixed(2);

        const topCategories = categories
            .sort((a, b) => b.total - a.total)
            .slice(0, 3)
            .map(c => c.category);

        document.getElementById('avg-expense').textContent = formatCurrency(avgExpense);
        document.getElementById('top-categories').textContent = topCategories.join(', ') || '-';

        if (allTransactions.length) {
            // --- Variaciones mensuales ---
            const monthly = {};
            allTransactions.forEach(t => {
                const ym = t.date.slice(0, 7);
                monthly[ym] = monthly[ym] || 0;
                monthly[ym] += t.type === 'EXPENSE' ? -t.amount : t.amount;
            });

            const sortedMonths = Object.keys(monthly).sort();
            const variations = [];
            for (let i = 1; i < sortedMonths.length; i++) {
                const prev = monthly[sortedMonths[i - 1]];
                const curr = monthly[sortedMonths[i]];
                const perc = prev ? ((curr - prev) / prev) * 100 : 0;
                variations.push({ month: sortedMonths[i], variation: perc.toFixed(2) + '%' });
            }
            document.getElementById('variations').textContent = variations.map(v => `${v.month}: ${v.variation}`).join(' | ') || '-';

            // --- GASTOS ATÍPICOS usando percentiles ---
            const expensesArray = allTransactions.filter(t => t.type === 'EXPENSE').map(t => t.amount).sort((a, b) => a - b);
            if (expensesArray.length > 0) {
                const percentile95Index = Math.floor(expensesArray.length * 0.95);
                const threshold = expensesArray[percentile95Index]; // umbral 95%

                const outliers = allTransactions.filter(t => t.type === 'EXPENSE' && t.amount >= threshold);
                document.getElementById('outliers').textContent = outliers.map(o => `${o.date} ${o.category} ${formatCurrency(o.amount)}`).join(' | ') || '-';
            } else {
                document.getElementById('outliers').textContent = '-';
            }
        }
    }



    // --- AUTOLOGIN ---
    if (localStorage.getItem('token')) {
        loginSection.style.display = 'none';
        dashboard.style.display = 'block';
        mainHeader.classList.remove('hide-header');
        await loadAllTransactions();
        await loadTransactions();
    } else {
        loginSection.style.display = 'flex';
        dashboard.style.display = 'none';
    }
});
