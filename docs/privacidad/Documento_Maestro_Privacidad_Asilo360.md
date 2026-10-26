# DOCUMENTO MAESTRO DE PRIVACIDAD, PROTECCIÓN DE DATOS PERSONALES Y DERECHOS ARCO

**Proyecto:** Asilo 360  
**Título del Documento:** Documento Maestro Integrado de Privacidad, Diagnóstico Técnico-Legal y Procedimientos ARCO  
**Responsable de Coordinación del Proyecto:** Aracely Guadalupe Perez Ramon  
**Rol en el Equipo:** Scrum Master  
**Correo de Contacto Institucional del Proyecto:** 2321082570@alumno.utpuebla.edu.mx  
**Fecha de Elaboración:** Febrero de 2025  
**Versión:** 1.0.0  
**Estatus de Validación:** *Documento sujeto a validación y dictamen por un profesional jurídico especializado previo a su publicación o uso comercial.*

---

## TABLA DE CONTENIDO (ÍNDICE)

*   **1. INTRODUCCIÓN**
    *   1.1. Propósito del Documento Maestro
    *   1.2. Visión General de la Plataforma Asilo 360
    *   1.3. Importancia de la Protección de Datos de los Adultos Mayores
*   **2. MARCO LEGAL APLICABLE**
    *   2.1. Normativa Mexicana de Protección de Datos (LFPDPPP y RLFPDPPP)
    *   2.2. Principios Rectores del Tratamiento
    *   2.3. Citas de Fuentes Oficiales
*   **3. INVENTARIO DE DATOS PERSONALES, SENSIBLES Y FINANCIEROS**
    *   3.1. Datos de Residentes (Adultos Mayores)
    *   3.2. Datos de Familiares
    *   3.3. Datos de Personal y Administradores
    *   3.4. Mapeo Técnico con Colecciones y Campos de Cloud Firestore
*   **4. RESPONSABILIDADES Y ROLES EN EL TRATAMIENTO DE DATOS**
    *   4.1. Deslinde de Funciones del Equipo Desarrollador y Scrum Master
    *   4.2. Definición del Responsable Legal del Tratamiento
    *   4.3. Marcadores de Posición Legal Pendientes
*   **5. AVISO DE PRIVACIDAD INTEGRAL**
    *   5.1. Identidad y Domicilio del Responsable
    *   5.2. Datos Personales Tratados y Sensibles de Salud
    *   5.3. Finalidades Primarias y Secundarias
    *   5.4. Transferencias de Datos y Proveedores Tecnológicos
    *   5.5. Mecanismos para Revocación y Derechos ARCO
*   **6. AVISO DE PRIVACIDAD SIMPLIFICADO**
    *   6.1. Versión Breve para Pantallas de Registro e Inicio de Sesión
    *   6.2. Leyenda y Redirección al Aviso Integral
*   **7. DERECHOS ARCO: GUÍA Y PROCEDIMIENTO DE ATENCIÓN**
    *   7.1. Definición de Derechos (Acceso, Rectificación, Cancelación, Oposición)
    *   7.2. Requisitos y Acreditación de Identidad y Representación Legal
    *   7.3. Etapas del Procedimiento y Plazos Legales (20 y 15 Días Hábiles)
    *   7.4. Causales de Negativa Legal
*   **8. FORMATO OFICIAL DE SOLICITUD DE DERECHOS ARCO**
    *   8.1. Formulario Estandarizado Listo para Imprimir o Completar
*   **9. SEGURIDAD, PROTECCIÓN TÉCNICA Y MEDIDAS DE CIBERSEGURIDAD (TOMs)**
    *   9.1. Cifrado y Protocolos en Tránsito (HTTPS/TLS)
    *   9.2. Reglas de Seguridad en Servidor (`firestore.rules`)
    *   9.3. Gestión de Incidentes de Seguridad
*   **10. EVALUACIÓN DE IMPACTO EN PRIVACIDAD (PIA) Y MATRIZ DE RIESGOS**
    *   10.1. Identificación de Riesgos y Controles Aplicados
*   **11. MATRIZ DE PERMISOS Y CONTROL DE ACCESO POR ROL**
    *   11.1. Tabla Comparativa de Permisos Realmente Verificados
*   **12. PRUEBAS AUTOMATIZADAS Y EVIDENCIAS DE VERIFICACIÓN TÉCNICA**
    *   12.1. Compilación y Unit Tests en Android (`assembleDebug`, `testDebugUnitTest`)
    *   12.2. Verificación de Tipos y Build en PWA (`tsc`, `vite build`)
*   **13. CHECKLIST DE PENDIENTES PREVIOS A LA PUBLICACIÓN COMERCIAL**
    *   13.1. Requisitos Legales, Técnicos y Operativos
*   **14. CONCLUSIONES Y RECOMENDACIONES EJECUTIVAS**
*   **15. FUENTES OFICIALES Y NORMATIVAS CONSULTADAS**

---

## 1. INTRODUCCIÓN

### 1.1. Propósito del Documento Maestro
El presente **Documento Maestro Integrado** unifica y sistematiza los análisis técnicos, diagnósticos de ciberseguridad, avisos de privacidad y procedimientos legales de atención a Derechos ARCO desarrollados para la plataforma **Asilo 360**. Su objetivo es garantizar que el tratamiento de la información personal cumpla estrictamente con la legislación mexicana en materia de protección de datos personales.

### 1.2. Visión General de la Plataforma Asilo 360
Asilo 360 es un ecosistema tecnológico diseñado para optimizar la gestión integral de residencias de adultos mayores y facilitar la comunicación transparente entre administradores, personal de cuidados médicos y familiares. El sistema opera a través de dos clientes desacoplados pero sincronizados en tiempo real:
*   **Aplicación Móvil Nativa Android:** Desarrollada en Kotlin con Jetpack Compose (`:app`).
*   **Aplicación Web Progresiva (PWA):** Construida con React 18, TypeScript 5, Vite 5 y Tailwind CSS (`asilo360-pwa`).
*   **Servicios Nube:** Autenticación de usuarios mediante **Firebase Authentication** y almacenamiento de datos en la nube mediante **Cloud Firestore**.

### 1.3. Importancia de la Protección de Datos de los Adultos Mayores
Los adultos mayores alojados en residencias geriátricas constituyen un grupo de especial vulnerabilidad. La información recopilada por Asilo 360 incluye diagnósticos médicos, prescripciones de medicamentos, bitácoras de constantes vitales, restricciones dietéticas y datos financieros. La protección, confidencialidad e integridad de estos datos personales sensibles es una prioridad técnica y un imperativo legal ético.

---

## 2. MARCO LEGAL APLICABLE

### 2.1. Normativa Mexicana de Protección de Datos
El tratamiento de datos personales realizado a través de Asilo 360 se rige bajo la legislación mexicana aplicable a personas físicas o morales del sector privado:
*   **Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP):** Publicada en el Diario Oficial de la Federación (DOF) el 5 de julio de 2010.
*   **Reglamento de la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (RLFPDPPP):** Publicado en el DOF el 21 de diciembre de 2011.
*   **Lineamientos del Aviso de Privacidad:** Emitidos por el Instituto Nacional de Transparencia, Acceso a la Información y Protección de Datos Personales (INAI) y publicados en el DOF el 17 de enero de 2013.

### 2.2. Principios Rectores del Tratamiento
De conformidad con el Artículo 6 de la LFPDPPP, Asilo 360 observa los principios rectores de:
1.  **Licitud:** Tratamiento con apego a las leyes mexicanas.
2.  **Consentimiento:** Obtención del consentimiento del titular o su tutor legal, especialmente para datos sensibles de salud (Art. 9 LFPDPPP).
3.  **Información:** Transparencia a través de Avisos de Privacidad.
4.  **Calidad:** Datos exactos, completos y actualizados.
5.  **Finalidad:** Tratamiento limitado exclusivamente a los fines pactados.
6.  **Lealtad:** Protección de la expectativa razonable de privacidad del titular.
7.  **Proporcionalidad:** Recopilación únicamente de los datos indispensables.
8.  **Responsabilidad:** Implementación de medidas de seguridad técnicas y administrativas.

---

## 3. INVENTARIO DE DATOS PERSONALES, SENSIBLES Y FINANCIEROS

A partir de la inspección del código fuente y las colecciones reales en Cloud Firestore, se estructuró el siguiente inventario:

| Categoría de Titular | Colecciones Firestore | Campos y Datos Recabados | Clasificación Legal |
| :--- | :--- | :--- | :--- |
| **Residente (Adulto Mayor)** | `residentes` | Nombre, apellidos, fecha de nacimiento, sexo, habitación, fecha de ingreso, estado de estancia. | Datos Personales Ordinarios |
| **Salud y Cuidado del Residente** | `consultas`, `actividades`, `residentes.observaciones` | Médico tratante, especialidad, motivo, diagnóstico clínico, tratamiento prescrito, receta médica, bitácora de medicamentos, signos vitales, alergias y dietas. | 🔴 **DATOS PERSONALES SENSIBLES DE SALUD** (Art. 9 LFPDPPP) |
| **Usuario Familiar** | `usuarios`, `familiares`, `residente_familiar` | Nombre, apellidos, correo electrónico, teléfono, relación de parentesco, identificadores (`id_usuario`, `id_familiar`). | Datos Personales Ordinarios |
| **Finanzas del Familiar** | `pagos` | Monto pagado ($ MXN), fecha de pago, concepto de mensualidad, método de pago (SPEI, Tarjeta, Efectivo) y estado. | 🟧 **DATOS FINANCIEROS Y PATRIMONIALES** (Art. 8 LFPDPPP) |
| **Personal / Empleados** | `personal`, `usuarios` | Nombre, apellidos, correo institucional, teléfono, puesto/cargo, turno asignado y estado laboral. | Datos Personales Ordinarios |

---

## 4. RESPONSABILIDADES Y ROLES EN EL TRATAMIENTO DE DATOS

### 4.1. Deslinde de Funciones del Equipo Desarrollador y Scrum Master
*   **Scrum Master y Coordinadora:** Aracely Guadalupe Perez Ramon (`2321082570@alumno.utpuebla.edu.mx`).
*   **Aclaración de Alcance:** Las funciones de la Scrum Master y del equipo de desarrollo son estrictamente de naturaleza técnica, metodológica y de arquitectura de software. **No constituyen la representación legal ni la titularidad del negocio.**

### 4.2. Definición del Responsable Legal del Tratamiento
Conforme al Artículo 3 Fracción XIV de la LFPDPPP, el **Responsable** es la persona física o moral de carácter privado (residencia geriátrica o empresa operadora) que decide sobre la determinación de las finalidades y medios del tratamiento de los datos personales.

### 4.3. Marcadores de Posición Legal Pendientes
Debido a que los datos comerciales definitivos deben ser provistos por la entidad que opere Asilo 360 en producción, se utilizan los siguientes marcadores:
*   `[RAZÓN SOCIAL O NOMBRE DEL RESPONSABLE LEGAL]`
*   `[DOMICILIO FISCAL LEGAL COMPLETO]`
*   `[URL PÚBLICA DEL AVISO DE PRIVACIDAD INTEGRAL]`

---

## 5. AVISO DE PRIVACIDAD INTEGRAL

### 5.1. Identidad y Domicilio del Responsable
El Responsable del tratamiento de sus datos personales es **[RAZÓN SOCIAL O NOMBRE DEL RESPONSABLE LEGAL]** (en adelante "El Responsable"), con domicilio fiscal en **[DOMICILIO FISCAL LEGAL COMPLETO, CALLE, NÚMERO, COLONIA, C.P., CIUDAD, ESTADO, MÉXICO]**.

Correo electrónico de contacto para temas de privacidad:  
📧 **2321082570@alumno.utpuebla.edu.mx** *(Sujeto a confirmación del canal oficial definitivo por el Responsable Legal)*.

### 5.2. Datos Personales Tratados y Sensibles de Salud
"El Responsable" recaba datos identificativos, de contacto, financieros y **Datos Personales Sensibles de Salud** (*diagnósticos médicos, historial de revisiones, recetas, bitácora de medicamentos y constantes vitales*). El tratamiento de los datos de salud se realiza con el consentimiento expreso y por escrito del titular o su tutor legal legítimo (Art. 9 LFPDPPP).

### 5.3. Finalidades Primarias y Secundarias
*   **Finalidades Primarias (Esenciales):**
    1.  Gestión del expediente residencial, estancia y asignación de habitación del adulto mayor.
    2.  Procesamiento y seguimiento de consultas médicas, diagnósticos y recetas.
    3.  Registro diario de actividades de cuidado, alimentación y administración de medicamentos.
    4.  Consulta autorizada en tiempo real para familiares vinculados.
    5.  Control y agendamiento de visitas presenciales.
    6.  Administración de cobros de cuotas mensuales y emisión de estados de cuenta.
    7.  Autenticación de acceso por roles (*Administrador*, *Personal*, *Familiar*).
*   **Finalidades Secundarias:**  
    Asilo 360 **NO utiliza** sus datos personales para fines publicitarios, mercadotécnicos ni de prospección comercial.

### 5.4. Transferencias de Datos y Proveedores Tecnológicos
Sus datos no serán transferidos a terceros sin su consentimiento, salvo las excepciones del Artículo 37 de la LFPDPPP (*requerimientos de autoridad sanitaria o urgencia médica*). Los servicios en la nube (**Google Cloud Platform / Firebase**) operan como Encargados bajo contratos con estrictas cláusulas de confidencialidad y cifrado.

### 5.5. Mecanismos para Revocación y Derechos ARCO
Usted puede ejercer sus derechos de Acceso, Rectificación, Cancelación u Oposición, o revocar su consentimiento, enviando una solicitud formal al correo `2321082570@alumno.utpuebla.edu.mx` siguiendo el procedimiento detallado en la Sección 7 de este documento.

---

## 6. AVISO DE PRIVACIDAD SIMPLIFICADO

**Aviso de Privacidad Simplificado para Formularios y Pantallas de Acceso**

> **[RAZÓN SOCIAL O NOMBRE DEL RESPONSABLE LEGAL]**, con domicilio en **[DOMICILIO FISCAL COMPLETO]**, utilizará sus datos personales (*identificación, contacto, expediente residencial, salud y mensualidades*) para gestionar el cuidado del residente, dar seguimiento médico, autorizar visitas y administrar cobros en Asilo 360.
> 
> Para conocer nuestro **Aviso de Privacidad Integral** completo y los mecanismos para ejercer sus **Derechos ARCO**, visite:  
> 🌐 `[URL_AVISO_PRIVACIDAD_INTEGRAL]`  
> 📧 Correo de Atención ARCO: `2321082570@alumno.utpuebla.edu.mx`

---

## 7. DERECHOS ARCO: GUÍA Y PROCEDIMIENTO DE ATENCIÓN

### 7.1. Definición de Derechos
*   **Acceso:** Conocer qué datos personales están almacenados en Firestore.
*   **Rectificación:** Solicitar la corrección de datos inexactos o desactualizados.
*   **Cancelación:** Solicitar la eliminación de datos cuando considere que no se requieren.
*   **Oposición:** Oponerse al tratamiento de sus datos para fines específicos.

### 7.2. Requisitos y Acreditación de Identidad
La solicitud debe enviarse al correo `2321082570@alumno.utpuebla.edu.mx` adjuntando:
1.  Nombre completo del titular y correo para respuesta.
2.  Copia de identificación oficial vigente (INE/Pasaporte).
3.  En caso de representar a un residente adulto mayor, adjuntar documento acreditativo de tutoría legal o poder notarial.
4.  Descripción clara de los datos y del derecho ARCO a ejercer.

### 7.3. Plazos Legales (Art. 32 LFPDPPP)
*   **Prevención por datos incompletos:** 5 días hábiles para requerir información faltante; el titular tiene 10 días hábiles para subsanar.
*   **Plazo para Comunicar la Respuesta:** Máximo **20 días hábiles** contados desde la recepción completa de la solicitud.
*   **Plazo para Hacer Efectiva la Respuesta:** Máximo **15 días hábiles** posteriores a la notificación de la respuesta procedente.

### 7.4. Causales de Negativa Legal (Art. 34 LFPDPPP)
La solicitud podrá ser negada si el solicitante no acredita la titularidad o representación, si los datos no existen en el sistema, o si existe mandato legal que exija conservar los expedientes médicos o fiscales.

---

## 8. FORMATO OFICIAL DE SOLICITUD DE DERECHOS ARCO

```text
================================================================================
          FORMATO OFICIAL DE SOLICITUD DE DERECHOS ARCO - ASILO 360
================================================================================

I. DATOS DEL TITULAR
Nombre Completo: _______________________________________________________________
Correo Electrónico: ____________________________________________________________
Teléfono: _______________________ Rol: [ ] Residente  [ ] Familiar  [ ] Personal

II. DATOS DEL REPRESENTANTE LEGAL / TUTOR (Solo si aplica)
Nombre del Representante: ______________________________________________________
Documento de Acreditación: [ ] Tutoría Legal  [ ] Poder Notarial  [ ] Acta

III. DERECHO SOLICITADO
[ ] ACCESO       [ ] RECTIFICACIÓN       [ ] CANCELACIÓN       [ ] OPOSICIÓN

IV. DESCRIPCIÓN DE LA SOLICITUD
________________________________________________________________________________
________________________________________________________________________________
________________________________________________________________________________

V. FIRMA Y FECHA
Lugar y Fecha: _________________________________________________________________
Firma del Solicitante: ________________________________________________________

Enviar este formato escaneado a: 2321082570@alumno.utpuebla.edu.mx
================================================================================
```

---

## 9. SEGURIDAD, PROTECCIÓN TÉCNICA Y MEDIDAS DE CIBERSEGURIDAD (TOMs)

1.  **Cifrado en Tránsito:** Conexiones cifradas mediante HTTPS y TLS 1.3 entre los clientes y Firebase.
2.  **Reglas de Seguridad en Servidor (`firestore.rules`):**
    *   `consultas` y `pagos`: Solo modificables por Administrador (`id_rol = 1`) y Personal (`id_rol = 2`).
    *   `residente_familiar`: Valida que los familiares solo lean expedientes expresamente vinculados.
3.  **Aislamiento de Entornos:** Aplicación web PWA independiente en `asilo360-pwa` con variables de entorno protegidas en `.env.local`.
4.  **Gestión de Contraseñas:** Autenticación delegada a Firebase Authentication. Respuestas neutras en recuperación de contraseña para evitar enumeración.

---

## 10. EVALUACIÓN DE IMPACTO EN PRIVACIDAD (PIA) Y MATRIZ DE RIESGOS

| Riesgo Detectado | Nivel de Impacto | Medida Técnica / Mitigación Implementada |
| :--- | :--- | :--- |
| Acceso no autorizado a diagnósticos médicos | 🔴 Crítico | Reglas de Firestore verifican la autenticación y el rol de usuario en cada petición. |
| Fuga de información entre familiares | 🔴 Crítico | Consulta estricta filtrada por la colección de enlace `residente_familiar`. |
| Modificación no autorizada de mensualidades | 🟧 Alto | Escritura en colección `pagos` permitida exclusivamente al rol Administrador. |
| Intentos de inicio de sesión por fuerza bruta | 🟨 Medio | Protección nativa de Firebase Auth con límites de intentos y bloqueos temporales. |

---

## 11. MATRIZ DE PERMISOS Y CONTROL DE ACCESO POR ROL

| Módulo / Colección Firestore | Administrador (`id_rol = 1`) | Personal (`id_rol = 2`) | Familiar Autorizado (`id_rol = 3`) |
| :--- | :--- | :--- | :--- |
| **`usuarios`** | Lectura / Escritura | Lectura de perfil | Lectura de perfil |
| **`residentes`** | Lectura / Escritura | Lectura de asignados | Lectura exclusiva de su familiar |
| **`consultas`** | Lectura / Escritura | Registro y actualización | Lectura de su familiar |
| **`actividades`** | Lectura / Escritura | Registro e ingreso diario | Lectura de su familiar |
| **`visitas`** | Lectura / Escritura | Confirmación e ingreso | Agendamiento y lectura propia |
| **`pagos`** | Lectura / Escritura | **Sin Acceso** | Lectura de cuotas de su familiar |
| **`residente_familiar`** | Lectura / Escritura | **Sin Acceso** | **Sin Acceso** |

---

## 12. PRUEBAS AUTOMATIZADAS Y EVIDENCIAS DE VERIFICACIÓN TÉCNICA

Los siguientes resultados provienen de la ejecución real de las suites de pruebas en el entorno de desarrollo:

1.  **Compilación Nativa Android (Gradle):**
    *   Comando: `./gradlew :app:assembleDebug`
    *   Resultado: **`BUILD SUCCESSFUL`** (0 errores).
2.  **Pruebas Unitarias Android:**
    *   Comando: `./gradlew :app:testDebugUnitTest`
    *   Resultado: **`1 passed, 0 failed`** (100% de éxito).
3.  **Verificación de Tipos TypeScript (PWA):**
    *   Comando: `npx tsc --noEmit`
    *   Resultado: **`0 errores`**.
4.  **Empaquetado de Producción Vite (PWA):**
    *   Comando: `npx vite build`
    *   Resultado: **`✓ built in 23.06s`** (0 errores).
5.  **Prueba de Conexión Servidor PWA:**
    *   Comando: `Invoke-WebRequest -Uri 'http://localhost:5173'`
    *   Resultado: **`StatusCode: 200 OK`**.

*Aclaración:* Estas pruebas técnicas confirman la estabilidad y compilación del software, pero no sustituyen la revisión legal formal del Responsable.

---

## 13. CHECKLIST DE PENDIENTES PREVIOS A LA PUBLICACIÓN COMERCIAL

- [ ] **Confirmación de Razón Social y Domicilio Fiscal** por la empresa/residencia operadora.
- [ ] **Creación del Correo Oficial Corporativo** de Privacidad (ej. `privacidad@asilo360.com`).
- [ ] **Publicación Web del Aviso de Privacidad** en la PWA (ej. `https://asilo360-app.web.app/privacidad`).
- [ ] **Firma y Dictamen Jurídico Externo** especializado en derecho digital y protección de datos en México.
- [ ] **Despliegue de Reglas de Seguridad** en Firebase Console (`firebase deploy --only firestore:rules`).
- [ ] **Firma del Ejecutable AAB** para Google Play Store (`./gradlew :app:bundleRelease`).

---

## 14. CONCLUSIONES Y RECOMENDACIONES EJECUTIVAS

1.  **Estado Técnico del Software:** Asilo 360 cuenta con una arquitectura limpia, desacoplada y funcional tanto en su versión nativa Android como en la PWA web, con control de acceso por roles verificado y compilación sin errores.
2.  **Estado de Privacidad:** La plataforma cuenta con el paquete completo de Avisos de Privacidad (Integral y Simplificado), Guía de Derechos ARCO y Evaluaciones de Riesgo específicas para la legislación mexicana.
3.  **Recomendación Prioritaria:** Antes de realizar la comercialización o recolectar datos reales de residentes en producción, el Responsable Legal de la residencia operadora debe revisar, completar los marcadores de posición y dictaminar formalmente estos documentos.

---

## 15. FUENTES OFICIALES Y NORMATIVAS CONSULTADAS

1.  **Cámara de Diputados del H. Congreso de la Unión:** *Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP)*. Texto vigente publicado en el Diario Oficial de la Federación el 5 de julio de 2010.  
    Enlace oficial: [http://www.diputados.gob.mx/LeyesBiblio/pdf/LFPDPPP.pdf](http://www.diputados.gob.mx/LeyesBiblio/pdf/LFPDPPP.pdf) (Consulta: Febrero 2025).
2.  **Presidencia de la República:** *Reglamento de la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (RLFPDPPP)*. Diario Oficial de la Federación el 21 de diciembre de 2011.  
    Enlace oficial: [http://www.diputados.gob.mx/LeyesBiblio/regley/Reg_LFPDPPP.pdf](http://www.diputados.gob.mx/LeyesBiblio/regley/Reg_LFPDPPP.pdf) (Consulta: Febrero 2025).
3.  **Instituto Nacional de Transparencia, Acceso a la Información y Protección de Datos Personales (INAI):** *Lineamientos del Aviso de Privacidad*. Diario Oficial de la Federación el 17 de enero de 2013.  
    Enlace oficial: [https://inicio.inai.org.mx/](https://inicio.inai.org.mx/) (Consulta: Febrero 2025).
