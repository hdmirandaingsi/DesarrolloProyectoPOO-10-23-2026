let authToken = sessionStorage.getItem('hotel_token') || '';
let currentUser = null;
let cacheClientes = [];
let cacheHabitaciones = [];
let cacheReservas = [];

document.addEventListener('DOMContentLoaded', () => {
    const hoy = new Date().toISOString().split('T')[0];
    const manana = new Date(Date.now() + 86400000 * 2).toISOString().split('T')[0];
    document.getElementById('disp-entrada').value = hoy;
    document.getElementById('disp-salida').value = manana;
    document.getElementById('res-entrada').value = hoy;
    document.getElementById('res-salida').value = manana;

    // Tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(btn.dataset.tab).classList.add('active');
        });
    });

    // Login Form
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('login-user').value.trim();
        const password = document.getElementById('login-pass').value;
        if (!username || !password) {
            mostrarAlertaLogin('Debe completar tanto el usuario/correo como la contraseña.', true);
            return;
        }
        const res = await apiPost('/api/login', { username, password });
        if (res.ok) {
            authToken = res.token;
            currentUser = res.usuario;
            sessionStorage.setItem('hotel_token', authToken);
            document.getElementById('login-pass').value = '';
            mostrarVistaProtegida();
        } else {
            mostrarAlertaLogin(res.error || 'Error de autenticación', true);
        }
    });

    // Logout
    document.getElementById('btn-logout').addEventListener('click', async () => {
        await apiPost('/api/logout', {});
        authToken = '';
        currentUser = null;
        sessionStorage.removeItem('hotel_token');
        document.getElementById('app-view').classList.add('hidden');
        document.getElementById('login-view').classList.remove('hidden');
        mostrarAlertaLogin('Sesión cerrada correctamente.', false);
    });

    // Form Clientes
    document.getElementById('form-cliente').addEventListener('submit', async (e) => {
        e.preventDefault();
        const accion = document.getElementById('cli-modo').value;
        const payload = {
            accion,
            codigo: document.getElementById('cli-codigo').value.trim(),
            nombre: document.getElementById('cli-nombre').value.trim(),
            documento: document.getElementById('cli-documento').value.trim(),
            telefono: document.getElementById('cli-telefono').value.trim(),
            email: document.getElementById('cli-email').value.trim()
        };
        const res = await apiPost('/api/clientes', payload);
        if (res.ok) {
            mostrarAlertaGlobal(res.mensaje, false);
            limpiarFormCliente();
            await cargarTodo();
        } else {
            mostrarAlertaGlobal(res.error, true);
        }
    });

    // Form Habitaciones
    document.getElementById('form-habitacion').addEventListener('submit', async (e) => {
        e.preventDefault();
        const accion = document.getElementById('hab-modo').value;
        const payload = {
            accion,
            codigo: document.getElementById('hab-codigo').value.trim(),
            numero: document.getElementById('hab-numero').value.trim(),
            tipo: document.getElementById('hab-tipo').value,
            capacidad: document.getElementById('hab-capacidad').value.trim(),
            precio: document.getElementById('hab-precio').value.trim(),
            estado: document.getElementById('hab-estado').value,
            comodidades: document.getElementById('hab-comodidades').value.trim()
        };
        const res = await apiPost('/api/habitaciones', payload);
        if (res.ok) {
            mostrarAlertaGlobal(res.mensaje, false);
            limpiarFormHabitacion();
            await cargarTodo();
        } else {
            mostrarAlertaGlobal(res.error, true);
        }
    });

    // Form Disponibilidad (Proceso 3)
    document.getElementById('form-disponibilidad').addEventListener('submit', async (e) => {
        e.preventDefault();
        await consultarDisponibilidad();
    });

    // Form Reservas (Proceso 4)
    document.getElementById('form-reserva').addEventListener('submit', async (e) => {
        e.preventDefault();
        const accion = document.getElementById('res-modo').value;
        const payload = {
            accion,
            codigo: document.getElementById('res-codigo').value.trim(),
            codigoCliente: document.getElementById('res-cliente').value,
            codigoHabitacion: document.getElementById('res-habitacion').value,
            entrada: document.getElementById('res-entrada').value,
            salida: document.getElementById('res-salida').value,
            huespedes: document.getElementById('res-huespedes').value,
            temporadaAlta: document.getElementById('res-temp-alta').checked ? 'true' : 'false'
        };
        const res = await apiPost('/api/reservas', payload);
        if (res.ok) {
            mostrarAlertaGlobal(res.mensaje, false);
            limpiarFormReserva();
            await cargarTodo();
        } else {
            mostrarAlertaGlobal(res.error, true);
        }
    });

    verificarSesionExistente();
});

async function verificarSesionExistente() {
    if (!authToken) return;
    const res = await apiGet('/api/session');
    if (res.ok) {
        currentUser = res.usuario;
        mostrarVistaProtegida();
    } else {
        authToken = '';
        sessionStorage.removeItem('hotel_token');
    }
}

async function mostrarVistaProtegida() {
    document.getElementById('login-view').classList.add('hidden');
    document.getElementById('app-view').classList.remove('hidden');
    document.getElementById('user-badge').textContent =
        `${currentUser.nombreCompleto} (${currentUser.rol})`;
    await cargarTodo();
    await consultarDisponibilidad();
}

async function cargarTodo() {
    await Promise.all([
        cargarDashboard(),
        cargarClientes(),
        cargarHabitaciones(),
        cargarReservas()
    ]);
    poblarSelectoresReserva();
}

async function cargarDashboard() {
    const res = await apiGet('/api/dashboard');
    if (!res.ok) return;
    const d = res.resumen;
    document.getElementById('kpi-clientes').textContent = d.totalClientes;
    document.getElementById('kpi-habitaciones').textContent =
        `${d.habDisponibles} / ${d.habOcupadas} / ${d.habFueraServicio}`;
    document.getElementById('kpi-reservas').textContent =
        `${d.resConfirmadas} / ${d.resCompletadas} / ${d.resCanceladas}`;
    document.getElementById('kpi-ingresos').textContent = `$${d.ingresosTotales.toFixed(2)}`;
    if (document.getElementById('res-modo').value === 'crear') {
        document.getElementById('res-codigo').value = d.siguienteCodigoReserva;
    }
}

async function cargarClientes() {
    const q = document.getElementById('buscar-cliente-input').value.trim();
    const res = await apiGet('/api/clientes?q=' + encodeURIComponent(q));
    if (!res.ok) return;
    cacheClientes = res.clientes;
    const tbody = document.getElementById('tabla-clientes-body');
    tbody.innerHTML = '';
    cacheClientes.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${c.codigoCliente}</strong></td>
            <td>${c.nombreCompleto}</td>
            <td>${c.documento}</td>
            <td>${c.telefono}</td>
            <td>${c.email}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="editarCliente('${c.codigoCliente}')">Editar</button>
                <button class="btn btn-danger btn-sm" onclick="eliminarCliente('${c.codigoCliente}')">Eliminar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function editarCliente(codigo) {
    const c = cacheClientes.find(x => x.codigoCliente === codigo);
    if (!c) return;
    document.getElementById('cli-modo').value = 'actualizar';
    document.getElementById('cli-codigo').value = c.codigoCliente;
    document.getElementById('cli-codigo').readOnly = true;
    document.getElementById('cli-nombre').value = c.nombreCompleto;
    document.getElementById('cli-documento').value = c.documento;
    document.getElementById('cli-telefono').value = c.telefono;
    document.getElementById('cli-email').value = c.email;
    document.getElementById('form-cliente-titulo').textContent = `Modificando Cliente ${c.codigoCliente}`;
}

function limpiarFormCliente() {
    document.getElementById('form-cliente').reset();
    document.getElementById('cli-modo').value = 'crear';
    document.getElementById('cli-codigo').readOnly = false;
    document.getElementById('form-cliente-titulo').textContent = '1.1 / 1.4 Registrar o Modificar Cliente';
}

async function eliminarCliente(codigo) {
    if (!confirm(`¿Confirma eliminar el cliente ${codigo}? Solo se permitirá si no tiene reservas asociadas.`)) return;
    const res = await apiPost('/api/clientes', { accion: 'eliminar', codigo });
    if (res.ok) {
        mostrarAlertaGlobal(res.mensaje, false);
        await cargarTodo();
    } else {
        mostrarAlertaGlobal(res.error, true);
    }
}

async function cargarHabitaciones() {
    const q = document.getElementById('buscar-hab-input').value.trim();
    const res = await apiGet('/api/habitaciones?q=' + encodeURIComponent(q));
    if (!res.ok) return;
    cacheHabitaciones = res.habitaciones;
    const tbody = document.getElementById('tabla-habitaciones-body');
    tbody.innerHTML = '';
    cacheHabitaciones.forEach(h => {
        const badgeClass = h.estado === 'DISPONIBLE' ? 'badge-success'
                         : (h.estado === 'OCUPADA' ? 'badge-warning' : 'badge-danger');
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${h.codigoHabitacion}</strong></td>
            <td>#${h.numero}</td>
            <td><span class="badge badge-info">${h.tipo}</span></td>
            <td>${h.capacidad} pers.</td>
            <td>$${h.precioNoche.toFixed(2)}</td>
            <td>$${h.cargoExtra.toFixed(2)}/noche</td>
            <td><span class="badge ${badgeClass}">${h.estado}</span></td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="editarHabitacion('${h.codigoHabitacion}')">Editar</button>
                <button class="btn btn-danger btn-sm" onclick="eliminarHabitacion('${h.codigoHabitacion}')">Eliminar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function editarHabitacion(codigo) {
    const h = cacheHabitaciones.find(x => x.codigoHabitacion === codigo);
    if (!h) return;
    document.getElementById('hab-modo').value = 'actualizar';
    document.getElementById('hab-codigo').value = h.codigoHabitacion;
    document.getElementById('hab-codigo').readOnly = true;
    document.getElementById('hab-numero').value = h.numero;
    document.getElementById('hab-tipo').value = h.tipo;
    document.getElementById('hab-capacidad').value = h.capacidad;
    document.getElementById('hab-precio').value = h.precioNoche;
    document.getElementById('hab-estado').value = h.estado;
    document.getElementById('hab-comodidades').value = h.comodidades;
    document.getElementById('form-hab-titulo').textContent = `Modificando Habitación ${h.codigoHabitacion}`;
}

function limpiarFormHabitacion() {
    document.getElementById('form-habitacion').reset();
    document.getElementById('hab-modo').value = 'crear';
    document.getElementById('hab-codigo').readOnly = false;
    document.getElementById('form-hab-titulo').textContent = '2.1 / 2.4 Registrar o Modificar Habitación';
}

async function eliminarHabitacion(codigo) {
    if (!confirm(`¿Confirma eliminar la habitación ${codigo}?`)) return;
    const res = await apiPost('/api/habitaciones', { accion: 'eliminar', codigo });
    if (res.ok) {
        mostrarAlertaGlobal(res.mensaje, false);
        await cargarTodo();
    } else {
        mostrarAlertaGlobal(res.error, true);
    }
}

async function consultarDisponibilidad() {
    const entrada = document.getElementById('disp-entrada').value;
    const salida = document.getElementById('disp-salida').value;
    const capacidad = document.getElementById('disp-capacidad').value;
    const tipo = document.getElementById('disp-tipo').value;
    const res = await apiGet(`/api/disponibilidad?entrada=${encodeURIComponent(entrada)}&salida=${encodeURIComponent(salida)}&capacidad=${encodeURIComponent(capacidad)}&tipo=${encodeURIComponent(tipo)}`);
    if (!res.ok) {
        mostrarAlertaGlobal(res.error, true);
        return;
    }
    const tbody = document.getElementById('tabla-disponibilidad-body');
    tbody.innerHTML = '';
    res.disponibles.forEach(h => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${h.codigoHabitacion}</strong></td>
            <td>#${h.numero}</td>
            <td><span class="badge badge-info">${h.tipo}</span></td>
            <td>${h.capacidad} pers.</td>
            <td>$${h.precioNoche.toFixed(2)}/noche</td>
            <td>${h.comodidades}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="cotizarEnDisponibilidad('${h.codigoHabitacion}')">Calcular Costo</button>
                <button class="btn btn-primary btn-sm" onclick="seleccionarParaReserva('${h.codigoHabitacion}')">Reservar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function cotizarEnDisponibilidad(codigoHabitacion) {
    const entrada = document.getElementById('disp-entrada').value;
    const salida = document.getElementById('disp-salida').value;
    const tempAlta = document.getElementById('disp-temp-alta').checked;
    const res = await apiGet(`/api/cotizar?codigoHabitacion=${encodeURIComponent(codigoHabitacion)}&entrada=${encodeURIComponent(entrada)}&salida=${encodeURIComponent(salida)}&temporadaAlta=${tempAlta}`);
    if (!res.ok) {
        mostrarAlertaGlobal(res.error, true);
        return;
    }
    const c = res.cotizacion;
    const box = document.getElementById('cotizacion-box');
    const det = document.getElementById('cotizacion-detalle');
    det.innerHTML = `
        <strong>Habitación #${c.numero} (${c.tipo})</strong> | Noches: <strong>${c.noches}</strong> | Temporada Alta: <strong>${c.temporadaAlta ? 'SÍ' : 'NO'}</strong><br>
        • Tarifa Base / Noche: <strong>$${c.tarifaBaseNoche.toFixed(2)}</strong><br>
        • Subtotal Base (${c.noches} × $${c.tarifaBaseNoche.toFixed(2)}): <strong>$${c.subtotalBase.toFixed(2)}</strong><br>
        • Cargos Polimórficos (Categoría / Temporada): <strong>$${c.cargosAdicionales.toFixed(2)}</strong><br>
        • <strong>COSTO TOTAL CALCULADO: $${c.costoTotal.toFixed(2)}</strong>
    `;
    box.classList.remove('hidden');
}

function seleccionarParaReserva(codigoHabitacion) {
    document.getElementById('res-habitacion').value = codigoHabitacion;
    document.getElementById('res-entrada').value = document.getElementById('disp-entrada').value;
    document.getElementById('res-salida').value = document.getElementById('disp-salida').value;
    document.getElementById('res-huespedes').value = document.getElementById('disp-capacidad').value;
    document.getElementById('res-temp-alta').checked = document.getElementById('disp-temp-alta').checked;
    document.querySelector('[data-tab="tab-reservas"]').click();
    previsualizarCostoReserva();
}

function poblarSelectoresReserva() {
    const selCli = document.getElementById('res-cliente');
    const selHab = document.getElementById('res-habitacion');
    const valCli = selCli.value;
    const valHab = selHab.value;

    selCli.innerHTML = '';
    cacheClientes.filter(c => c.activo).forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.codigoCliente;
        opt.textContent = `${c.codigoCliente} - ${c.nombreCompleto} (${c.documento})`;
        selCli.appendChild(opt);
    });

    selHab.innerHTML = '';
    cacheHabitaciones.forEach(h => {
        const opt = document.createElement('option');
        opt.value = h.codigoHabitacion;
        opt.textContent = `${h.codigoHabitacion} - Hab. #${h.numero} [${h.tipo}] Cap:${h.capacidad} ($${h.precioNoche.toFixed(2)}/noche) - ${h.estado}`;
        selHab.appendChild(opt);
    });

    if (valCli) selCli.value = valCli;
    if (valHab) selHab.value = valHab;
}

async function previsualizarCostoReserva() {
    const codigoHabitacion = document.getElementById('res-habitacion').value;
    const entrada = document.getElementById('res-entrada').value;
    const salida = document.getElementById('res-salida').value;
    const tempAlta = document.getElementById('res-temp-alta').checked;
    const res = await apiGet(`/api/cotizar?codigoHabitacion=${encodeURIComponent(codigoHabitacion)}&entrada=${encodeURIComponent(entrada)}&salida=${encodeURIComponent(salida)}&temporadaAlta=${tempAlta}`);
    const preview = document.getElementById('res-cotizacion-preview');
    if (!res.ok) {
        mostrarAlertaGlobal(res.error, true);
        preview.classList.add('hidden');
        return;
    }
    const c = res.cotizacion;
    preview.innerHTML = `
        <strong>Proceso 5 - Cálculo de Costo:</strong> ${c.noches} noche(s) × $${c.tarifaBaseNoche.toFixed(2)} =
        Subtotal Base <strong>$${c.subtotalBase.toFixed(2)}</strong> + Cargos Extra <strong>$${c.cargosAdicionales.toFixed(2)}</strong>
        = <strong>TOTAL: $${c.costoTotal.toFixed(2)}</strong>
    `;
    preview.classList.remove('hidden');
}

async function cargarReservas() {
    const q = document.getElementById('buscar-res-input').value.trim();
    const estado = document.getElementById('filtro-res-estado').value;
    const res = await apiGet(`/api/reservas?q=${encodeURIComponent(q)}&estado=${encodeURIComponent(estado)}`);
    if (!res.ok) return;
    cacheReservas = res.reservas;
    const tbody = document.getElementById('tabla-reservas-body');
    tbody.innerHTML = '';
    cacheReservas.forEach(r => {
        const badgeClass = r.estado === 'CONFIRMADA' ? 'badge-success'
                         : (r.estado === 'COMPLETADA' ? 'badge-info' : 'badge-danger');
        const acciones = r.estado === 'CONFIRMADA'
            ? `<button class="btn btn-secondary btn-sm" onclick="editarReserva('${r.codigoReserva}')">Modificar</button>
               <button class="btn btn-success btn-sm" onclick="completarReserva('${r.codigoReserva}')">Completar</button>
               <button class="btn btn-danger btn-sm" onclick="cancelarReserva('${r.codigoReserva}')">Cancelar</button>`
            : `<small>Cerrada</small>`;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${r.codigoReserva}</strong></td>
            <td>${r.nombreCliente}<br><small>${r.codigoCliente}</small></td>
            <td>#${r.numeroHabitacion} (${r.tipoHabitacion})<br><small>${r.codigoHabitacion}</small></td>
            <td>${r.fechaEntrada} → ${r.fechaSalida}</td>
            <td>${r.noches} noche(s)<br><small>${r.cantidadHuespedes} huésp. | TA: ${r.temporadaAlta ? 'Sí' : 'No'}</small></td>
            <td>Base: $${r.subtotalBase.toFixed(2)}<br><small>Extra: $${r.cargosAdicionales.toFixed(2)}</small></td>
            <td><strong>$${r.costoTotal.toFixed(2)}</strong></td>
            <td><span class="badge ${badgeClass}">${r.estado}</span></td>
            <td>${acciones}</td>
        `;
        tbody.appendChild(tr);
    });
}

function editarReserva(codigo) {
    const r = cacheReservas.find(x => x.codigoReserva === codigo);
    if (!r) return;
    document.getElementById('res-modo').value = 'actualizar';
    document.getElementById('res-codigo').value = r.codigoReserva;
    document.getElementById('res-codigo').readOnly = true;
    document.getElementById('res-cliente').value = r.codigoCliente;
    document.getElementById('res-cliente').disabled = true;
    document.getElementById('res-habitacion').value = r.codigoHabitacion;
    document.getElementById('res-entrada').value = r.fechaEntrada;
    document.getElementById('res-salida').value = r.fechaSalida;
    document.getElementById('res-huespedes').value = r.cantidadHuespedes;
    document.getElementById('res-temp-alta').checked = r.temporadaAlta;
    document.getElementById('form-res-titulo').textContent = `Modificando Reserva ${r.codigoReserva}`;
}

function limpiarFormReserva() {
    document.getElementById('form-reserva').reset();
    document.getElementById('res-modo').value = 'crear';
    document.getElementById('res-codigo').readOnly = false;
    document.getElementById('res-cliente').disabled = false;
    document.getElementById('form-res-titulo').textContent = '4.1 / 4.6 Registrar o Modificar Reserva';
    document.getElementById('res-cotizacion-preview').classList.add('hidden');
    cargarDashboard();
}

async function cancelarReserva(codigo) {
    if (!confirm(`¿Confirma cancelar la reserva ${codigo}?`)) return;
    const res = await apiPost('/api/reservas', { accion: 'cancelar', codigo });
    if (res.ok) {
        mostrarAlertaGlobal(res.mensaje, false);
        await cargarTodo();
    } else {
        mostrarAlertaGlobal(res.error, true);
    }
}

async function completarReserva(codigo) {
    const res = await apiPost('/api/reservas', { accion: 'completar', codigo });
    if (res.ok) {
        mostrarAlertaGlobal(res.mensaje, false);
        await cargarTodo();
    } else {
        mostrarAlertaGlobal(res.error, true);
    }
}

function mostrarAlertaLogin(msg, isError) {
    const el = document.getElementById('login-alert');
    el.textContent = msg;
    el.className = 'alert ' + (isError ? 'alert-error' : 'alert-success');
    el.classList.remove('hidden');
}

function mostrarAlertaGlobal(msg, isError) {
    const el = document.getElementById('global-alert');
    el.textContent = msg;
    el.className = 'alert ' + (isError ? 'alert-error' : 'alert-success');
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 6000);
}

async function apiGet(url) {
    const r = await fetch(url, {
        headers: { 'X-Auth-Token': authToken }
    });
    return await r.json();
}

async function apiPost(url, data) {
    const body = Object.keys(data)
        .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(data[k] ?? ''))
        .join('&');
    const r = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'X-Auth-Token': authToken
        },
        body
    });
    return await r.json();
}
