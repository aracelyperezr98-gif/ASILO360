# EVALUACIÓN DE IMPACTO EN PRIVACIDAD Y ANÁLISIS DE RIESGOS

**Proyecto:** Asilo 360  
**Fecha:** Febrero de 2025  
**Documento Técnico-Legal de Ciberseguridad y Protección de Datos Personales**

---

## 1. EVALUACIÓN DE RIESGOS TÉCNICOS Y OPERATIVOS

| Amenaza / Riesgo | Nivel de Impacto | Medida de Control Técnica Implementada |
| :--- | :--- | :--- |
| **Acceso No Autorizado a Datos de Salud** | 🔴 Crítico | Restricción por reglas de Firestore en servidor (`firestore.rules`) y verificación de rol e `id_usuario` en repositorio. |
| **Fuga de Información entre Familiares** | 🔴 Crítico | Validación estricta del vínculo en la colección `residente_familiar`. Un familiar sin vínculo recibe pantalla vacía sin datos. |
| **Enumeración de Usuarios en Login** | 🟧 Alto | Respuestas neutras en la función de restablecimiento de contraseña (`sendPasswordResetEmail`), evitando revelar si el correo existe. |
| **Inyección o Alteración de Datos Financieros** | 🟧 Alto | Reglas de Firestore restringen la creación y modificación de documentos en la colección `pagos` exclusivamente al rol Administrador (`id_rol = 1`). |
| **Pérdida de Sesión en Dispositivo Móvil** | 🟨 Medio | Mantenimiento en memoria mediante `UserSessionManager` y recuperación reactiva del perfil en Firestore tras autenticación con Firebase Auth. |

---

## 2. MEDIDAS DE SEGURIDAD TÉCNICAS, FÍSICAS Y ADMINISTRATIVAS (TOMs)

### A. Medidas Técnicas
1.  **Cifrado en Tránsito:** Todas las comunicaciones entre los clientes (Android / PWA) y los servidores de Firebase se realizan exclusivamente a través de protocolos seguros **HTTPS / TLS 1.3**.
2.  **Autenticación Robusta:** Gestión de tokens JWT y sesiones mediante el servicio certificado **Firebase Authentication**.
3.  **Filtrado en Servidor:** Las reglas de seguridad de Cloud Firestore (`firestore.rules`) validan los permisos de lectura y escritura directamente en el servidor NoSQL antes de devolver cualquier documento.
4.  **Aislamiento de Entornos:** La PWA fue desacoplada en su propio proyecto independiente (`asilo360-pwa`) para evitar exposición accidental de dependencias o scripts no deseados.
5.  **Control de Secretos:** Las variables de entorno de Firebase Web SDK se almacenan en `.env.local` fuera del control de versiones.

### B. Medidas Administrativas
1.  **Principio de Menor Privilegio:** Cada usuario asignado a un rol (*Administrador*, *Personal*, *Familiar*) cuenta únicamente con los accesos indispensables para realizar sus funciones.
2.  **Supervisión del Scrum Master:** La gestión y coordinación del proyecto es supervisada por Aracely Guadalupe Perez Ramon, manteniendo control de versiones y auditorías de código.

---

## 3. PROCEDIMIENTO DE ATENCIÓN A INCIDENTES DE SEGURIDAD

En caso de detectarse un incidente de seguridad o vulneración que afecte datos personales o de salud:

1.  **Detección y Contención (0 - 2 horas):** Aislar la colección o cuenta afectada, revocando tokens de acceso en Firebase Authentication.
2.  **Evaluación de Impacto (2 - 12 horas):** Analizar los registros de Firestore para determinar qué categorías de datos e individuos fueron involucrados.
3.  **Notificación a Titulares y Autoridades:** Comunicar de forma inmediata a los titulares afectados y, en su caso, a la autoridad garante (INAI) conforme a los plazos legales aplicables.
4.  **Remediación y Correctivos:** Aplicar parches de código, actualizar reglas de seguridad e implementar controles adicionales antes de restablecer el servicio.
