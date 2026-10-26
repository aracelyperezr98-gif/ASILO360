# INVENTARIO DE DATOS PERSONALES Y FINALIDADES DEL TRATAMIENTO

**Proyecto:** Asilo 360  
**Versión:** 1.0.0  
**Fecha de Elaboración:** Febrero de 2025  
**Marco Normativo de Referencia:** Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP) y su Reglamento (RLFPDPPP).

---

## 1. INTRODUCCIÓN Y ALCANCE

Este documento constituye el inventario técnico y legal de las categorías de datos personales, datos personales sensibles y datos financieros o patrimoniales que son recabados, almacenados y procesados a través de las plataformas móvil (Android) y web (PWA) de **Asilo 360**.

---

## 2. INVENTARIO DETALLADO POR TIPO DE TITULAR Y COLECCIÓN FIRESTORE

### A. Titulares: Residentes de la Residencia (Adultos Mayores)
*   **Colección en Firestore:** `residentes`
*   **Datos Identificativos y Generales:**
    *   Nombre(s), apellido paterno, apellido materno.
    *   Fecha de nacimiento (utilizada para el cálculo de edad).
    *   Sexo.
    *   Número de habitación asignada.
    *   Fecha de ingreso a la residencia.
    *   Estado de estancia (*Activo*, *Inactivo*, *Baja*).
*   **Datos Personales Sensibles (Salud y Estado de Cuidado):**
    *   **Colección `consultas`:** Médico tratante, especialidad médica, motivo de consulta, diagnóstico clínico, tratamiento prescrito, receta médica e indicaciones.
    *   **Colección `actividades`:** Bitácora diaria de cuidado, administración de medicamentos, control de signos vitales, dieta especial, higiene personal y terapias físicas.
    *   **Campo `observaciones` (en `residentes`):** Alergias, padecimientos preexistentes, restricciones alimenticias e indicaciones especiales de cuidado.

### B. Titulares: Usuarios con Rol Familiar
*   **Colecciones en Firestore:** `usuarios`, `familiares`, `residente_familiar`
*   **Datos Identificativos y de Contacto:**
    *   Nombre(s), apellido paterno, apellido materno.
    *   Correo electrónico (utilizado como credencial de acceso).
    *   Número telefónico de contacto.
    *   Relación de parentesco con el residente (*Hijo/a, Cónyuge, Hermano/a, Nieto/a, Tutor Legal, Otro*).
    *   Identificador único del perfil (`id_usuario`, `id_familiar`).
*   **Datos Financieros y Patrimoniales:**
    *   **Colección `pagos`:** Registro de cuotas mensuales de estancia, monto pagado ($ MXN), fecha de pago, concepto de cobranza y método de pago (*SPEI, Tarjeta, Efectivo, Depósito*).

### C. Titulares: Personal de la Residencia (Empleados y Cuidadores)
*   **Colección en Firestore:** `personal`, `usuarios`
*   **Datos Identificativos y Laborales:**
    *   Nombre(s), apellido paterno, apellido materno.
    *   Correo electrónico institucional / laboral.
    *   Teléfono de contacto.
    *   Puesto o cargo (*Enfermero/a, Médico General, Cuidador/a, Fisioterapeuta, Administrativo*).
    *   Turno asignado (*Matutino, Vespertino, Nocturno*).
    *   Estado laboral (*Activo / Inactivo*).

---

## 3. FINALIDADES DEL TRATAMIENTO

### A. Finalidades Primarias (Necesarias para la prestación del servicio)
1.  **Gestión de la Estancia Residencial:** Administrar la asignación de habitaciones, ingresos, permanencia y expediente general del adulto mayor.
2.  **Seguimiento Médico y Cuidado Integral:** Registrar consultas médicas, diagnósticos, recetas, administración puntual de medicamentos y bitácoras de constantes vitales.
3.  **Comunicación con Familiares Autorizados:** Notificar en tiempo real a los familiares autorizados sobre el estado de salud, avisos, visitas programadas y estados de cuenta.
4.  **Control de Visitas:** Agendar, confirmar y supervisar el ingreso presencial de familiares y visitantes a las instalaciones.
5.  **Gestión Financiera y Cobranza:** Emitir estados de cuenta, registrar comprobantes de pago de mensualidades y controlar adeudos.
6.  **Control de Acceso y Seguridad:** Autenticar a los usuarios en las plataformas Android y PWA asegurando que cada rol acceda únicamente a la información autorizada.

### B. Finalidades Secundarias (No esenciales para el servicio)
Actualmente, **Asilo 360 NO utiliza los datos personales para finalidades secundarias** tales como mercadotecnia, publicidad, prospección comercial ni perfilamiento comercial de terceros. En caso de implementar alguna finalidad secundaria en el futuro, se habilitará el mecanismo de negativación respectivo.

---

## 4. PERMISOS Y MATRIZ DE ACCESO POR ROL EN EL SISTEMA

| Módulo / Datos | Administrador (`id_rol = 1`) | Personal (`id_rol = 2`) | Familiar Autorizado (`id_rol = 3`) |
| :--- | :--- | :--- | :--- |
| **Expediente Residente** | CRUD Completo | Lectura de asignados | Lectura exclusiva de su familiar vinculado |
| **Diagnósticos Médicos** | CRUD Completo | Registro y actualización | Lectura autorizada |
| **Bitácora de Actividades** | CRUD Completo | Registro e ingreso diario | Lectura del familiar vinculado |
| **Visitas Programadas** | CRUD Completo | Confirmación de recepción | Agendamiento y lectura propia |
| **Estados de Cuenta / Pagos**| CRUD Completo | Sin Acceso | Lectura de cuotas de su familiar |
| **Vínculos Familiares** | CRUD Completo | Sin Acceso | Sin Acceso |

---

## 5. TIEMPOS DE CONSERVACIÓN Y POLÍTICA DE RETENCIÓN

1.  **Datos Clínicos y de Cuidado:** Conservación obligatoria durante la permanencia del residente y por el plazo mínimo establecido por la regulación sanitaria mexicana aplicable (NOI/NOM del sector salud).
2.  **Registros Contables y Financieros:** Conservación durante 5 años conforme a las disposiciones del Código Fiscal de la Federación.
3.  **Cuentas Inactivas o Canceladas:** En caso de retiro de un vínculo o baja de usuario, la información pasa a estado de **bloqueo** previo a su supresión definitiva, salvo que exista obligación legal de conservación.
