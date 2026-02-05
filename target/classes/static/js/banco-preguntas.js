// Estado global
let allQuestions = [];
let deleteId = null;
const questionModal = new bootstrap.Modal(document.getElementById('questionModal'));
const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
const toast = new bootstrap.Toast(document.getElementById('liveToast'));

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    setupCsrf();
    loadBlocks();
    loadQuestions();

    // Configurar confirmación de borrado
    document.getElementById('confirmDeleteBtn').addEventListener('click', () => {
        if (deleteId) {
            deleteQuestion(deleteId);
        }
    });
});

function setupCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) {
        axios.defaults.headers.common[header] = token;
    }
}

// Cargar bloques para desplegables
async function loadBlocks() {
    try {
        const response = await axios.get('/api/blocks');
        const blocks = response.data;
        const select = document.getElementById('blockSelect');
        const filter = document.getElementById('filterBlock');
        
        // Limpiar existentes (mantener primera opción)
        select.innerHTML = '<option value="" selected disabled>Selecciona un bloque</option>';
        filter.innerHTML = '<option value="">Todos los Bloques</option>';

        blocks.forEach(block => {
            // Añadir al selector de la ventana
            const option = document.createElement('option');
            option.value = block.id;
            option.textContent = block.name;
            select.appendChild(option);

            // Añadir al filtro
            const filterOption = document.createElement('option');
            filterOption.value = block.name; // Filtrar por nombre para simplificar el filtrado en frontend
            filterOption.textContent = block.name;
            filter.appendChild(filterOption);
        });
    } catch (error) {
        showToast('Error', 'No se pudieron cargar los bloques.', 'text-danger');
        console.error(error);
    }
}

// Cargar preguntas
async function loadQuestions() {
    try {
        const response = await axios.get('/api/questions');
        allQuestions = response.data;
        // Aplicar filtros actuales en lugar de renderizar todo
        filterQuestions();
    } catch (error) {
        showToast('Error', 'No se pudieron cargar las preguntas.', 'text-danger');
        console.error(error);
    }
}

async function duplicateQuestion(id) {
    try {
        await axios.post(`/api/questions/${id}/duplicate`);
        showToast('Éxito', 'Pregunta duplicada correctamente.', 'text-success');
        loadQuestions();
    } catch (error) {
        showToast('Error', 'No se pudo duplicar la pregunta.', 'text-danger');
        console.error(error);
    }
}

// Renderizar tabla de preguntas
function renderQuestions(questions) {
    const tbody = document.getElementById('questionsTableBody');
    tbody.innerHTML = '';

    if (questions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-5 text-muted">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <p>No hay preguntas todavía. ¡Crea una nueva!</p>
                </td>
            </tr>`;
        return;
    }

    questions.forEach((q, index) => {
        const tr = document.createElement('tr');
        
        // Formatear opciones con la correcta resaltada
        let optionsHtml = '<ul class="list-unstyled mb-0 small">';
        optionsHtml += `<li class="${q.correctOption === 1 ? 'text-success fw-bold' : ''}">${q.correctOption === 1 ? '<i class="fas fa-check me-1"></i>' : ''} ${escapeHtml(q.option1)}</li>`;
        optionsHtml += `<li class="${q.correctOption === 2 ? 'text-success fw-bold' : ''}">${q.correctOption === 2 ? '<i class="fas fa-check me-1"></i>' : ''} ${escapeHtml(q.option2)}</li>`;
        optionsHtml += `<li class="${q.correctOption === 3 ? 'text-success fw-bold' : ''}">${q.correctOption === 3 ? '<i class="fas fa-check me-1"></i>' : ''} ${escapeHtml(q.option3)}</li>`;
        optionsHtml += `<li class="${q.correctOption === 4 ? 'text-success fw-bold' : ''}">${q.correctOption === 4 ? '<i class="fas fa-check me-1"></i>' : ''} ${escapeHtml(q.option4)}</li>`;
        optionsHtml += '</ul>';

        tr.innerHTML = `
            <td class="ps-4 text-muted">${index + 1}</td>
            <td><div class="fw-bold text-wrap" style="max-width: 300px;">${escapeHtml(q.text)}</div></td>
            <td><span class="badge bg-light text-dark border">${escapeHtml(q.blockName || '-')}</span></td>
            <td>${optionsHtml}</td>
            <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-info me-1" onclick="duplicateQuestion(${q.id})" title="Duplicar">
                    <i class="fas fa-copy"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary me-1" onclick="prepareEditModal(${q.id})" title="Editar">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="prepareDelete(${q.id})" title="Eliminar">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Lógica de filtrado
function filterQuestions() {
    const searchText = document.getElementById('searchInput').value.toLowerCase();
    const blockFilter = document.getElementById('filterBlock').value;

    const filtered = allQuestions.filter(q => {
        const matchesText = q.text.toLowerCase().includes(searchText) || 
                            q.option1.toLowerCase().includes(searchText) ||
                            q.option2.toLowerCase().includes(searchText) ||
                            q.option3.toLowerCase().includes(searchText) ||
                            q.option4.toLowerCase().includes(searchText);
        
        const matchesBlock = blockFilter === '' || (q.blockName && q.blockName === blockFilter);

        return matchesText && matchesBlock;
    });

    renderQuestions(filtered);
}

// Ayudas de la ventana
function prepareCreateModal() {
    document.getElementById('questionForm').reset();
    document.getElementById('questionId').value = '';
    document.getElementById('blockSelectContainer').style.display = 'block'; // Mostrar selector de bloque
    document.getElementById('questionModalLabel').textContent = 'Nueva Pregunta';
}

function prepareEditModal(id) {
    const question = allQuestions.find(q => q.id === id);
    if (!question) return;

    document.getElementById('questionId').value = question.id;
    document.getElementById('blockSelectContainer').style.display = 'none'; // Ocultar selector de bloque
    
    // Forzar conversión a cadena para coincidencia robusta
    const blockSelect = document.getElementById('blockSelect');
    blockSelect.value = String(question.blockId);
    
    // Debug: comprobar si la selección funcionó
    if (!blockSelect.value) {
        console.warn(`Block ID ${question.blockId} not found in dropdown options`, blockSelect.options);
        // Intentar alternativa buscando la opción iterando (por si hay desajuste de tipo o espacios)
        for (let i = 0; i < blockSelect.options.length; i++) {
            if (blockSelect.options[i].value == question.blockId) {
                blockSelect.selectedIndex = i;
                break;
            }
        }
    }

    document.getElementById('questionText').value = question.text;
    document.getElementById('option1').value = question.option1;
    document.getElementById('option2').value = question.option2;
    document.getElementById('option3').value = question.option3;
    document.getElementById('option4').value = question.option4;
    
    // Seleccionar botón de opción
    const radios = document.getElementsByName('correctOption');
    for (const radio of radios) {
        if (parseInt(radio.value) === question.correctOption) {
            radio.checked = true;
            break;
        }
    }

    document.getElementById('questionModalLabel').textContent = 'Editar Pregunta';
    questionModal.show();
}

// Operaciones CRUD
async function saveQuestion() {
    const id = document.getElementById('questionId').value;
    const blockId = document.getElementById('blockSelect').value;
    const text = document.getElementById('questionText').value.trim();
    const option1 = document.getElementById('option1').value.trim();
    const option2 = document.getElementById('option2').value.trim();
    const option3 = document.getElementById('option3').value.trim();
    const option4 = document.getElementById('option4').value.trim();
    
    let correctOption = null;
    document.getElementsByName('correctOption').forEach(r => {
        if (r.checked) correctOption = parseInt(r.value);
    });

    // Validación
    if (!blockId || !text || !option1 || !option2 || !option3 || !option4 || !correctOption) {
        showToast('Atención', 'Por favor completa todos los campos requeridos.', 'text-warning');
        return;
    }

    const payload = {
        blockId: parseInt(blockId),
        text,
        option1,
        option2,
        option3,
        option4,
        correctOption
    };

    try {
        if (id) {
            // Actualizar
            await axios.put(`/api/questions/${id}`, payload);
            showToast('Éxito', 'Pregunta actualizada correctamente.', 'text-success');
        } else {
            // Crear
            await axios.post('/api/questions', payload);
            showToast('Éxito', 'Pregunta creada correctamente.', 'text-success');
        }
        
        questionModal.hide();
        loadQuestions();
    } catch (error) {
        showToast('Error', 'No se pudo guardar la pregunta.', 'text-danger');
        console.error(error);
    }
}

function prepareDelete(id) {
    deleteId = id;
    deleteModal.show();
}

async function deleteQuestion(id) {
    try {
        await axios.delete(`/api/questions/${id}`);
        showToast('Eliminado', 'La pregunta ha sido eliminada.', 'text-success');
        deleteModal.hide();
        loadQuestions();
    } catch (error) {
        showToast('Error', 'No se pudo eliminar la pregunta.', 'text-danger');
        console.error(error);
    }
}

// Utilidades
function showToast(title, message, colorClass) {
    const titleEl = document.getElementById('toastTitle');
    const messageEl = document.getElementById('toastMessage');
    if (!titleEl || !messageEl) {
        alert(`${title}: ${message}`);
        return;
    }
    titleEl.textContent = title;
    titleEl.className = `me-auto fw-bold ${colorClass || ''}`;
    messageEl.textContent = message;
    toast.show();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.innerText = text;
    return div.innerHTML;
}