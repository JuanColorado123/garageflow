/**
 * seed.mjs
 * Carga datos de ejemplo en el sistema GarageFlow.
 * 
 * Env vars:
 *   ORDENES_URL: URL de ms-ordenes (default: http://ms-ordenes:8081)
 *   AGENDA_URL: URL de ms-agenda (default: http://ms-agenda:8082)
 *   AUDITORIA_URL: URL de ms-auditoria (default: http://ms-auditoria:8083)
 *   FORZAR: si es "1", carga datos incluso si ya existen
 *   TZ: zona horaria (default: America/Bogota)
 * 
 * Requisito: Node 22+, sin dependencias npm (usa fetch global y global APIs)
 */

const ORDENES_URL = process.env.ORDENES_URL || 'http://ms-ordenes:8081';
const AGENDA_URL = process.env.AGENDA_URL || 'http://ms-agenda:8082';
const AUDITORIA_URL = process.env.AUDITORIA_URL || 'http://ms-auditoria:8083';
const FORZAR = process.env.FORZAR === '1';
const TIMEOUT_MS = 10000;

/**
 * Espera a que un servicio esté disponible (status UP)
 */
async function esperarServicioHasta(url, nombreServicio, maxTiempoMs = 5 * 60 * 1000) {
  const tiempoInicio = Date.now();
  const intervaloMs = 3000;

  while (Date.now() - tiempoInicio < maxTiempoMs) {
    try {
      const resp = await fetch(`${url}/actuator/health`, {
        signal: AbortSignal.timeout(TIMEOUT_MS),
      });
      if (resp.ok) {
        const data = await resp.json();
        if (data.status === 'UP') {
          return;
        }
      }
    } catch (e) {
      // servicio no disponible, reintenta
    }
    await new Promise(resolve => setTimeout(resolve, intervaloMs));
  }

  throw new Error(`${nombreServicio} no respondió UP en 5 minutos`);
}

/**
 * Llamada genérica a la API con manejo de errores
 */
async function llamadaAPI(metodo, url, cuerpo = null, usuarioHeader = 'asesor.ana') {
  const opciones = {
    method: metodo,
    headers: {
      'Content-Type': 'application/json',
      'X-Usuario': usuarioHeader,
      'X-Rol': 'ASESOR',
    },
    signal: AbortSignal.timeout(TIMEOUT_MS),
  };

  if (cuerpo) {
    opciones.body = JSON.stringify(cuerpo);
  }

  const resp = await fetch(url, opciones);
  let textoRespuesta = '';
  try {
    textoRespuesta = await resp.text();
  } catch (e) {
    textoRespuesta = '(no se pudo leer el cuerpo)';
  }

  if (!resp.ok) {
    const err = new Error(
      `${metodo} ${url} -> ${resp.status}: ${textoRespuesta}`
    );
    err.status = resp.status;
    throw err;
  }

  if (textoRespuesta) {
    try {
      return JSON.parse(textoRespuesta);
    } catch (e) {
      // no es JSON, devuelve el texto
      return textoRespuesta;
    }
  }
  return null;
}



/**
 * Reabre una orden (solo desde REPARADA o CERRADA)
 */
async function reabrirOrden(ordenId, motivo, usuario) {
  await llamadaAPI('POST', `${ORDENES_URL}/api/ordenes/${ordenId}/reabrir`, { motivo }, usuario);
}

/**
 * Calcula la fecha local en formato YYYY-MM-DD, desde la fecha actual más diasAdelante
 */
function formatoFechaLocal(diasAdelante = 0) {
  const año = new Date().getFullYear();
  const mes = new Date().getMonth() + 1;
  const día = new Date().getDate();
  const fecha = new Date(año, mes - 1, día + diasAdelante);
  
  const añoFormato = fecha.getFullYear();
  const mesFormato = String(fecha.getMonth() + 1).padStart(2, '0');
  const díaFormato = String(fecha.getDate()).padStart(2, '0');
  
  return `${añoFormato}-${mesFormato}-${díaFormato}`;
}

/**
 * Lleva una orden a un estado objetivo llamando a avanzar repetidamente
 */
async function llevarA(ordenId, estadoObjetivo, usuario) {
  let respuesta = await llamadaAPI('GET', `${ORDENES_URL}/api/ordenes/${ordenId}`);
  let estadoActual = respuesta.estado;

  const transiciones = {
    REGISTRADA: 'EN_DIAGNOSTICO',
    EN_DIAGNOSTICO: 'EN_REPARACION',
    EN_REPARACION: 'REPARADA',
    REPARADA: 'CERRADA',
  };

  while (estadoActual !== estadoObjetivo) {
    const proxEst = transiciones[estadoActual];
    if (!proxEst) {
      throw new Error(`No hay transición desde ${estadoActual} a ${estadoObjetivo}`);
    }
    respuesta = await llamadaAPI('POST', `${ORDENES_URL}/api/ordenes/${ordenId}/avanzar`, {}, usuario);
    estadoActual = respuesta.estado;
  }

  return respuesta;
}

/**
 * Obtiene disponibilidad de citas para una fecha
 */
async function obtenerDisponibilidad(fecha) {
  return await llamadaAPI(
    'GET',
    `${AGENDA_URL}/api/agenda/disponibilidad?fecha=${fecha}`
  );
}

/**
 * Busca y reserva una cita en un slot disponible
 */
async function buscarYReservarCita(ordenId, tipo, usuario, diasBusqueda = 30) {
  let diaOffset = 1; // Empezar mañana

  while (diaOffset <= diasBusqueda) {
    const fechaStr = formatoFechaLocal(diaOffset);
    
    // Omite domingos (weekday 0 = Sunday)
    const año = new Date().getFullYear();
    const mes = new Date().getMonth() + 1;
    const día = new Date().getDate();
    const fecha = new Date(año, mes - 1, día + diaOffset);
    if (fecha.getDay() === 0) {
      diaOffset++;
      continue;
    }

    const disponibilidad = await obtenerDisponibilidad(fechaStr);
    
    // Busca un slot con libres > 0
    for (const slot of disponibilidad) {
      if (slot.libres && slot.libres > 0) {
        try {
          const cita = await llamadaAPI(
            'POST',
            `${AGENDA_URL}/api/agenda/citas`,
            {
              ordenId,
              asesorId: slot.asesorId,
              tipo,
              franjaInicio: slot.franjaInicio,
            },
            usuario
          );
          return cita; // Éxito
        } catch (e) {
          // Solo 409 (sin cupo) continúa; cualquier otro error se propaga
          if (e.status === 409) {
            continue;
          }
          throw e;
        }
      }
    }
    diaOffset++;
  }

  throw new Error(`No se encontró disponibilidad en ${diasBusqueda} días`);
}

/**
 * Punto de entrada principal
 */
async function main() {
  try {
    // Paso 1: esperar a que los servicios estén UP
    console.log('DATOS_EJEMPLO: esperando servicios...');
    await esperarServicioHasta(ORDENES_URL, 'ms-ordenes');
    await esperarServicioHasta(AGENDA_URL, 'ms-agenda');
    await esperarServicioHasta(AUDITORIA_URL, 'ms-auditoria');
    console.log('DATOS_EJEMPLO: servicios listos');

    // Paso 2: verificar si ya hay datos
    const ordenesExistentes = await llamadaAPI('GET', `${ORDENES_URL}/api/ordenes`);
    if (ordenesExistentes && ordenesExistentes.length > 0 && !FORZAR) {
      console.log(`DATOS_EJEMPLO: ya hay datos (${ordenesExistentes.length} órdenes), no se carga nada`);
      process.exit(0);
    }

    // Paso 3: crear órdenes
    const ordenes = [];
    const ordenesEjemplo = [
      // 1. REGISTRADA, particular (asesor.ana)
      {
        clienteNombre: 'Juan Ramírez',
        clienteTelefono: '+573051234567',
        vehiculoPlaca: 'ABC123',
        vehiculoDescripcion: 'Toyota Corolla 2020, blanco',
        aseguradora: '',
        trabajo: 'Revisión general y cambio de aceite',
        usuarioCreador: 'asesor.ana',
        estadoFinal: 'REGISTRADA',
      },
      // 2. EN_DIAGNOSTICO, aseguradora Seguros Bolívar (asesor.luis)
      {
        clienteNombre: 'María López García',
        clienteTelefono: '+573157654321',
        vehiculoPlaca: 'XYZ789',
        vehiculoDescripcion: 'Honda Civic 2019, gris',
        aseguradora: 'Seguros Bolívar',
        trabajo: 'Reparación de sistema de frenos',
        usuarioCreador: 'asesor.luis',
        estadoFinal: 'EN_DIAGNOSTICO',
      },
      // 3. EN_REPARACION, aseguradora Sura (asesor.ana)
      {
        clienteNombre: 'Pedro Martínez',
        clienteTelefono: '+573001112222',
        vehiculoPlaca: 'DEF456',
        vehiculoDescripcion: 'Mazda 3 2021, rojo',
        aseguradora: 'Sura',
        trabajo: 'Cambio de batería y alternador',
        usuarioCreador: 'asesor.ana',
        estadoFinal: 'EN_REPARACION',
      },
      // 4. REPARADA, particular (asesor.luis)
      {
        clienteNombre: 'Carolina Rodríguez',
        clienteTelefono: '+573109876543',
        vehiculoPlaca: 'GHI111',
        vehiculoDescripcion: 'Hyundai Elantra 2022, azul',
        aseguradora: '',
        trabajo: 'Alineación y balanceo de llantas',
        usuarioCreador: 'asesor.luis',
        estadoFinal: 'REPARADA',
      },
      // 5. CERRADA, particular (asesor.ana) - avanzar a REPARADA, agendar cita TRAER, luego cerrar
      {
        clienteNombre: 'David Sánchez',
        clienteTelefono: '+573212345678',
        vehiculoPlaca: 'JKL222',
        vehiculoDescripcion: 'Nissan Sentra 2020, negro',
        aseguradora: '',
        trabajo: 'Reparación de aire acondicionado',
        usuarioCreador: 'asesor.ana',
        estadoFinal: 'CERRADA',
        conCitaTraerAntes: true,
      },
      // 6. Reabierta de CERRADA (asesor.luis)
      {
        clienteNombre: 'Roberto Fernández',
        clienteTelefono: '+573156789012',
        vehiculoPlaca: 'MNO333',
        vehiculoDescripcion: 'Chevrolet Spark 2021, blanco',
        aseguradora: '',
        trabajo: 'Cambio de pastillas de freno y discos',
        usuarioCreador: 'asesor.luis',
        estadoFinal: 'EN_REPARACION',
        reabierta: true,
      },
      // 7. EN_DIAGNOSTICO, aseguradora Allianz (asesor.ana)
      {
        clienteNombre: 'Patricia Gutiérrez',
        clienteTelefono: '+573051111111',
        vehiculoPlaca: 'PQR444',
        vehiculoDescripcion: 'Ford Focus 2018, gris oscuro',
        aseguradora: 'Allianz',
        trabajo: 'Diagnóstico de luz de motor encendida',
        usuarioCreador: 'asesor.ana',
        estadoFinal: 'EN_DIAGNOSTICO',
      },
    ];

    for (const ejOrden of ordenesEjemplo) {
      const orden = await llamadaAPI(
        'POST',
        `${ORDENES_URL}/api/ordenes`,
        {
          clienteNombre: ejOrden.clienteNombre,
          clienteTelefono: ejOrden.clienteTelefono,
          vehiculoPlaca: ejOrden.vehiculoPlaca,
          vehiculoDescripcion: ejOrden.vehiculoDescripcion,
          aseguradora: ejOrden.aseguradora,
          trabajo: ejOrden.trabajo,
        },
        ejOrden.usuarioCreador
      );
      ordenes.push({
        id: orden.id,
        placa: ejOrden.vehiculoPlaca,
        estadoFinal: ejOrden.estadoFinal,
        usuario: ejOrden.usuarioCreador,
        conCitaTraerAntes: ejOrden.conCitaTraerAntes,
        reabierta: ejOrden.reabierta,
      });
    }

    // Avanza cada orden al estado requerido
    for (let idx = 0; idx < ordenes.length; idx++) {
      const ord = ordenes[idx];
      
      if (ord.reabierta) {
        // Orden 6: lleva a CERRADA, reabre, lleva a EN_REPARACION
        await llevarA(ord.id, 'CERRADA', ord.usuario);
        await reabrirOrden(
          ord.id,
          'El cliente reporta ruido en la suspensión después de la entrega',
          ord.usuario
        );
        await llevarA(ord.id, 'EN_REPARACION', ord.usuario);
      } else if (ord.conCitaTraerAntes) {
        // Orden 5: lleva a REPARADA, agenda cita, luego cierra
        await llevarA(ord.id, 'REPARADA', ord.usuario);
      } else {
        // Resto: lleva directamente al estado final
        await llevarA(ord.id, ord.estadoFinal, ord.usuario);
      }
    }

    // Paso 4: agendar citas
    let citasCreadas = 0;

    // Cita TRAER para orden 1 (REGISTRADA)
    await buscarYReservarCita(ordenes[0].id, 'TRAER', 'asesor.ana');
    citasCreadas++;

    // Cita TRAER para orden 2 (EN_DIAGNOSTICO)
    await buscarYReservarCita(ordenes[1].id, 'TRAER', 'asesor.luis');
    citasCreadas++;

    // Cita TRAER para orden 5 (REPARADA), antes de cerrar
    await buscarYReservarCita(ordenes[4].id, 'TRAER', 'asesor.ana');
    citasCreadas++;

    // Cierra orden 5 ahora que tiene su cita TRAER (cierre cancela la cita sin avisar)
    await llevarA(ordenes[4].id, 'CERRADA', ordenes[4].usuario);

    // Cita TRAER para orden 7 (EN_DIAGNOSTICO)
    await buscarYReservarCita(ordenes[6].id, 'TRAER', 'asesor.ana');
    citasCreadas++;

    // Cita RECOGER para orden 4 (REPARADA)
    await buscarYReservarCita(ordenes[3].id, 'RECOGER', 'asesor.luis');
    citasCreadas++;

    // Una cita TRAER para orden 3 (EN_REPARACION), luego cancelarla con motivo MANUAL
    const citaParaCancelar = await buscarYReservarCita(ordenes[2].id, 'TRAER', 'asesor.ana');
    citasCreadas++;
    // Cancela la cita
    await llamadaAPI(
      'POST',
      `${AGENDA_URL}/api/agenda/citas/${citaParaCancelar.id}/cancelar`,
      { motivo: 'MANUAL' },
      'asesor.ana'
    );

    // Reporte final: obtener datos reales de cada orden
    for (const ord of ordenes) {
      const ordenData = await llamadaAPI('GET', `${ORDENES_URL}/api/ordenes/${ord.id}`);
      console.log(`DATOS_EJEMPLO: orden ${ordenData.vehiculoPlaca} -> ${ordenData.estado}`);
    }

    console.log(`DATOS_EJEMPLO: listo, ${ordenes.length} órdenes y ${citasCreadas} citas`);
    process.exit(0);

  } catch (error) {
    console.error(`DATOS_EJEMPLO: ERROR ${error.message}`);
    process.exit(1);
  }
}

main();
