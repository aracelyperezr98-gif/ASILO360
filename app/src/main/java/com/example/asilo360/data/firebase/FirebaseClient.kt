package com.example.asilo360.data.firebase

import com.example.asilo360.data.model.ActualizarPerfilRequest
import com.example.asilo360.data.model.CambiarEstadoConsultaRequest
import com.example.asilo360.data.model.CambiarEstadoPagoRequest
import com.example.asilo360.data.model.CambiarEstadoRequest
import com.example.asilo360.data.model.CambiarEstadoVisitaRequest
import com.example.asilo360.data.model.CambiarPasswordRequest
import com.example.asilo360.data.model.ConfiguracionOperationResponse
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.ConsultaOperationResponse
import com.example.asilo360.data.model.ConsultasListResponse
import com.example.asilo360.data.model.CrearNotificacionRequest
import com.example.asilo360.data.model.EstadoCantidad
import com.example.asilo360.data.model.FamiliarOpcion
import com.example.asilo360.data.model.FamiliaresPorResidenteResponse
import com.example.asilo360.data.model.FinancieroReporteData
import com.example.asilo360.data.model.MarcarLeidaRequest
import com.example.asilo360.data.model.MesCantidad
import com.example.asilo360.data.model.MesMonto
import com.example.asilo360.data.model.MetodoPagoResumen
import com.example.asilo360.data.model.Notificacion
import com.example.asilo360.data.model.NotificacionOperationResponse
import com.example.asilo360.data.model.NotificacionesListResponse
import com.example.asilo360.data.model.Pago
import com.example.asilo360.data.model.PagoOperationResponse
import com.example.asilo360.data.model.PagosListResponse
import com.example.asilo360.data.model.PerfilResponse
import com.example.asilo360.data.model.PerfilUsuario
import com.example.asilo360.data.model.Personal
import com.example.asilo360.data.model.PersonalListResponse
import com.example.asilo360.data.model.PersonalOperationResponse
import com.example.asilo360.data.model.ReporteDashboardResponse
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.ResidenteOperationResponse
import com.example.asilo360.data.model.ResidentesListResponse
import com.example.asilo360.data.model.ResidentesReporteData
import com.example.asilo360.data.model.ResumenKpi
import com.example.asilo360.data.model.Usuario
import com.example.asilo360.data.model.UsuarioDestinatarioOpcion
import com.example.asilo360.data.model.UsuariosListResponse
import com.example.asilo360.data.model.Visita
import com.example.asilo360.data.model.VisitaOperationResponse
import com.example.asilo360.data.model.VisitasListResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirebaseResult<T>(private val value: T) {
    val isSuccessful: Boolean = true
    fun body(): T = value
}

object FirebaseClient {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val apiService = UsersService()
    val residentesApiService = ResidentsService()
    val personalApiService = StaffService()
    val actividadesApiService = ActivitiesService()
    val consultasApiService = ConsultationsService()
    val visitasApiService = VisitsService()
    val pagosApiService = PaymentsService()
    val notificacionesApiService = NotificationsService()
    val reportesApiService = ReportsService()
    val configuracionApiService = ProfileService()

    class UsersService {
        suspend fun listarUsuarios(): FirebaseResult<UsuariosListResponse> = FirebaseResult(
            UsuariosListResponse(success = true, data = readUsers())
        )
    }

    class ResidentsService {
        suspend fun listarResidentes(): FirebaseResult<ResidentesListResponse> = FirebaseResult(
            ResidentesListResponse(success = true, data = readResidents())
        )

        suspend fun crearResidente(residente: Residente): FirebaseResult<ResidenteOperationResponse> {
            val id = insert("residentes", "id_residente", residente.asMap())
            return FirebaseResult(ResidenteOperationResponse(success = true, message = "Residente registrado correctamente.", data = com.example.asilo360.data.model.ResidenteCreatedData(id)))
        }

        suspend fun actualizarResidente(residente: Residente): FirebaseResult<ResidenteOperationResponse> {
            update("residentes", "id_residente", residente.id_residente, residente.asMap())
            return FirebaseResult(ResidenteOperationResponse(success = true, message = "Residente actualizado correctamente."))
        }

        suspend fun eliminarResidente(id: Int): FirebaseResult<ResidenteOperationResponse> {
            deleteRelated("residente_familiar", "id_residente", id)
            deleteRelated("consultas", "id_residente", id)
            deleteRelated("visitas", "id_residente", id)
            deleteRelated("pagos", "id_residente", id)
            deleteRelated("actividades", "id_residente", id)
            db.collection("residentes").document(id.toString()).delete().await()
            return FirebaseResult(ResidenteOperationResponse(success = true, message = "Residente eliminado correctamente."))
        }
    }

    class StaffService {
        suspend fun listarPersonal(): FirebaseResult<PersonalListResponse> = FirebaseResult(
            PersonalListResponse(success = true, personal = readStaff())
        )

        suspend fun crearPersonal(personal: Personal): FirebaseResult<PersonalOperationResponse> {
            require(auth.currentUser != null) { "Inicia sesión para administrar personal." }
            val password = personal.password?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Ingresa una contraseña para crear la cuenta de personal.")
            val mainApp = FirebaseApp.getInstance()
            val secondaryApp = try {
                FirebaseApp.getInstance(STAFF_AUTH_APP)
            } catch (_: IllegalStateException) {
                FirebaseApp.initializeApp(mainApp.applicationContext, mainApp.options, STAFF_AUTH_APP)
                    ?: throw IllegalStateException("No se pudo inicializar Firebase Authentication para el nuevo usuario.")
            }
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            val newUser = secondaryAuth.createUserWithEmailAndPassword(personal.correo, password).await().user
                ?: throw IllegalStateException("Firebase no devolvió la cuenta nueva.")

            try {
                val userId = nextId("usuarios", "id_usuario")
                val staffId = nextId("personal", "id_personal")
                val batch = db.batch()
                batch.set(
                    db.collection("usuarios").document(newUser.uid),
                    mapOf(
                        "id_usuario" to userId,
                        "nombre" to personal.nombre,
                        "apellido_paterno" to personal.apellido_paterno,
                        "apellido_materno" to personal.apellido_materno,
                        "correo" to personal.correo,
                        "telefono" to personal.telefono,
                        "id_rol" to 2,
                        "rol" to "Personal",
                        "estado" to 1,
                        "fecha_registro" to nowString()
                    )
                )
                batch.set(
                    db.collection("personal").document(staffId.toString()),
                    mapOf(
                        "id_personal" to staffId,
                        "id_usuario" to userId,
                        "puesto" to personal.puesto,
                        "fecha_ingreso" to personal.fecha_ingreso,
                        "estado" to personal.estado
                    )
                )
                batch.commit().await()
                return FirebaseResult(
                    PersonalOperationResponse(
                        success = true,
                        message = "Personal registrado correctamente.",
                        id_personal = staffId,
                        id_usuario = userId
                    )
                )
            } catch (exception: Exception) {
                try {
                    newUser.delete().await()
                } catch (cleanupException: Exception) {
                    exception.addSuppressed(cleanupException)
                }
                throw exception
            }
        }

        suspend fun actualizarPersonal(personal: Personal): FirebaseResult<PersonalOperationResponse> {
            update("personal", "id_personal", personal.id_personal, personal.asMap())
            return FirebaseResult(PersonalOperationResponse(success = true, message = "Personal actualizado correctamente."))
        }

        suspend fun cambiarEstado(request: CambiarEstadoRequest): FirebaseResult<PersonalOperationResponse> {
            update("personal", "id_personal", request.id_personal, mapOf("estado" to request.estado))
            return FirebaseResult(PersonalOperationResponse(success = true, message = "Estado actualizado correctamente."))
        }
    }

    class ActivitiesService {
        suspend fun listarActividades(): FirebaseResult<com.example.asilo360.data.model.ActividadesListResponse> {
            val residents = readResidents().associateBy { it.id_residente }
            val staff = readStaff().associateBy { it.id_personal }
            val actividades = readDocuments("actividades").map { document ->
                val act = document.toActividad()
                val resident = residents[act.id_residente]
                val worker = staff[act.id_personal]
                act.copy(
                    residente = resident?.nombreCompleto(),
                    habitacion = resident?.habitacion,
                    personal = worker?.nombreCompleto()
                )
            }
            return FirebaseResult(com.example.asilo360.data.model.ActividadesListResponse(success = true, data = actividades))
        }

        suspend fun crearActividad(actividad: com.example.asilo360.data.model.Actividad): FirebaseResult<com.example.asilo360.data.model.ActividadOperationResponse> {
            val id = insert("actividades", "id_actividad", actividad.asMap())
            return FirebaseResult(com.example.asilo360.data.model.ActividadOperationResponse(success = true, message = "Actividad registrada correctamente.", id_actividad = id))
        }

        suspend fun actualizarActividad(actividad: com.example.asilo360.data.model.Actividad): FirebaseResult<com.example.asilo360.data.model.ActividadOperationResponse> {
            update("actividades", "id_actividad", actividad.id_actividad, actividad.asMap())
            return FirebaseResult(com.example.asilo360.data.model.ActividadOperationResponse(success = true, message = "Actividad actualizada correctamente."))
        }

        suspend fun eliminarActividad(id: Int): FirebaseResult<com.example.asilo360.data.model.ActividadOperationResponse> {
            db.collection("actividades").document(id.toString()).delete().await()
            return FirebaseResult(com.example.asilo360.data.model.ActividadOperationResponse(success = true, message = "Actividad eliminada correctamente."))
        }
    }

    class ConsultationsService {
        suspend fun listarConsultas(): FirebaseResult<ConsultasListResponse> {
            val residents = readResidents().associateBy { it.id_residente }
            val staff = readStaff().associateBy { it.id_personal }
            val consultas = readDocuments("consultas").map { document ->
                val consulta = document.toConsulta()
                val resident = residents[consulta.id_residente]
                val worker = staff[consulta.id_personal]
                consulta.copy(
                    nombre_residente = resident?.nombreCompleto(),
                    nombre_personal = worker?.nombreCompleto(),
                    puesto_personal = worker?.puesto
                )
            }
            return FirebaseResult(ConsultasListResponse(success = true, consultas = consultas))
        }

        suspend fun crearConsulta(consulta: Consulta): FirebaseResult<ConsultaOperationResponse> {
            val id = insert("consultas", "id_consulta", consulta.asMap())
            return FirebaseResult(ConsultaOperationResponse(success = true, message = "Consulta registrada correctamente.", id_consulta = id))
        }

        suspend fun actualizarConsulta(consulta: Consulta): FirebaseResult<ConsultaOperationResponse> {
            update("consultas", "id_consulta", consulta.id_consulta, consulta.asMap())
            return FirebaseResult(ConsultaOperationResponse(success = true, message = "Consulta actualizada correctamente."))
        }

        suspend fun cambiarEstado(request: CambiarEstadoConsultaRequest): FirebaseResult<ConsultaOperationResponse> {
            update("consultas", "id_consulta", request.id_consulta, mapOf("estado" to request.estado))
            return FirebaseResult(ConsultaOperationResponse(success = true, message = "Estado actualizado correctamente."))
        }
    }

    class VisitsService {
        suspend fun listarVisitas(): FirebaseResult<VisitasListResponse> {
            val residents = readResidents().associateBy { it.id_residente }
            val families = readFamilies().associateBy { it.id_familiar }
            val users = readUsers().associateBy { it.id_usuario }
            val visits = readDocuments("visitas").map { document ->
                val visit = document.toVisita()
                val resident = residents[visit.id_residente]
                val family = families[visit.id_familiar]
                val user = family?.let { users[it.id_usuario] }

                val nomRes = visit.nombre_residente?.takeIf { it.isNotBlank() } ?: resident?.nombreCompleto()
                val hab = visit.habitacion?.takeIf { it.isNotBlank() } ?: resident?.habitacion
                val nomFam = visit.nombre_familiar?.takeIf { it.isNotBlank() } ?: user?.nombreCompleto()
                val par = visit.parentesco?.takeIf { it.isNotBlank() } ?: family?.parentesco

                visit.copy(
                    nombre_residente = nomRes,
                    habitacion = hab,
                    nombre_familiar = nomFam,
                    parentesco = par
                )
            }
            return FirebaseResult(VisitasListResponse(success = true, visitas = visits))
        }

        suspend fun obtenerFamiliaresPorResidente(idResidente: Int): FirebaseResult<FamiliaresPorResidenteResponse> {
            val relationDocs = db.collection("residente_familiar")
                .whereEqualTo("id_residente", idResidente).get().await().documents
            val familyById = readFamilies().associateBy { it.id_familiar }
            val userById = readUsers().associateBy { it.id_usuario }
            val options = relationDocs.mapNotNull { relation ->
                val familyId = relation.int("id_familiar")
                val family = familyById[familyId] ?: return@mapNotNull null
                val user = userById[family.id_usuario] ?: return@mapNotNull null
                FamiliarOpcion(
                    id_familiar = family.id_familiar,
                    id_usuario = user.id_usuario,
                    nombre = user.nombreCompleto(),
                    parentesco = relation.text("parentesco").ifBlank { family.parentesco },
                    telefono = family.telefono.ifBlank { user.telefono.orEmpty() }
                )
            }
            return FirebaseResult(FamiliaresPorResidenteResponse(success = true, familiares = options))
        }

        suspend fun crearVisita(visita: Visita): FirebaseResult<VisitaOperationResponse> {
            val id = insert("visitas", "id_visita", visita.asMap())
            return FirebaseResult(VisitaOperationResponse(success = true, message = "Visita registrada correctamente.", id_visita = id))
        }

        suspend fun actualizarVisita(visita: Visita): FirebaseResult<VisitaOperationResponse> {
            update("visitas", "id_visita", visita.id_visita, visita.asMap())
            return FirebaseResult(VisitaOperationResponse(success = true, message = "Visita actualizada correctamente."))
        }

        suspend fun cambiarEstado(request: CambiarEstadoVisitaRequest): FirebaseResult<VisitaOperationResponse> {
            update("visitas", "id_visita", request.id_visita, mapOf("estado" to request.estado))
            return FirebaseResult(VisitaOperationResponse(success = true, message = "Estado actualizado correctamente."))
        }
    }

    class PaymentsService {
        suspend fun listarPagos(): FirebaseResult<PagosListResponse> {
            val residentNames = readResidents().associate { it.id_residente to it.nombreCompleto() }
            val payments = readDocuments("pagos").map { document ->
                val payment = document.toPago()
                payment.copy(residente = residentNames[payment.id_residente])
            }
            return FirebaseResult(PagosListResponse(success = true, pagos = payments))
        }

        suspend fun crearPago(pago: Pago): FirebaseResult<PagoOperationResponse> {
            val id = insert("pagos", "id_pago", pago.asMap())
            return FirebaseResult(PagoOperationResponse(success = true, message = "Pago registrado correctamente.", id_pago = id))
        }

        suspend fun actualizarPago(pago: Pago): FirebaseResult<PagoOperationResponse> {
            update("pagos", "id_pago", pago.id_pago, pago.asMap())
            return FirebaseResult(PagoOperationResponse(success = true, message = "Pago actualizado correctamente."))
        }

        suspend fun cambiarEstado(request: CambiarEstadoPagoRequest): FirebaseResult<PagoOperationResponse> {
            update("pagos", "id_pago", request.id_pago, mapOf("estado" to request.estado))
            return FirebaseResult(PagoOperationResponse(success = true, message = "Estado actualizado correctamente."))
        }

        suspend fun eliminarPago(id: Int): FirebaseResult<PagoOperationResponse> {
            db.collection("pagos").document(id.toString()).delete().await()
            return FirebaseResult(PagoOperationResponse(success = true, message = "Pago eliminado correctamente."))
        }
    }

    class NotificationsService {
        suspend fun listarNotificaciones(): FirebaseResult<NotificacionesListResponse> {
            val users = readUsers().associateBy { it.id_usuario }
            val notifications = readDocuments("notificaciones").map { document ->
                val notification = document.toNotificacion()
                val user = users[notification.id_usuario]
                notification.copy(destinatario = user?.nombreCompleto(), rol = user?.rol)
            }
            return FirebaseResult(NotificacionesListResponse(success = true, notificaciones = notifications))
        }

        suspend fun crearNotificacion(request: CrearNotificacionRequest): FirebaseResult<NotificacionOperationResponse> {
            val allUsers = readUsers()
            val recipients = request.id_usuario?.let { id -> allUsers.filter { it.id_usuario == id } }
                ?: allUsers.filter { user ->
                    request.destinatario_tipo.isNullOrBlank() ||
                        request.destinatario_tipo.equals("Todos", true) ||
                        request.destinatario_tipo.equals(user.rol, true) ||
                        request.destinatario_tipo.equals("${user.id_rol}", true)
                }
            require(recipients.isNotEmpty()) { "No hay usuarios que coincidan con el destinatario." }
            var lastId = 0
            recipients.forEach { user ->
                lastId = insert(
                    "notificaciones",
                    "id_notificacion",
                    mapOf(
                        "id_usuario" to user.id_usuario,
                        "titulo" to request.titulo,
                        "mensaje" to request.mensaje,
                        "tipo" to request.tipo,
                        "leida" to false,
                        "fecha" to nowString()
                    )
                )
            }
            return FirebaseResult(NotificacionOperationResponse(success = true, message = "Notificación enviada correctamente.", id_notificacion = lastId))
        }

        suspend fun marcarLeida(request: MarcarLeidaRequest): FirebaseResult<NotificacionOperationResponse> {
            update("notificaciones", "id_notificacion", request.id_notificacion, mapOf("leida" to true))
            return FirebaseResult(NotificacionOperationResponse(success = true, message = "Notificación marcada como leída."))
        }

        suspend fun eliminarNotificacion(id: Int): FirebaseResult<NotificacionOperationResponse> {
            db.collection("notificaciones").document(id.toString()).delete().await()
            return FirebaseResult(NotificacionOperationResponse(success = true, message = "Notificación eliminada correctamente."))
        }
    }

    class ReportsService {
        suspend fun obtenerDashboardReportes(
            fechaInicio: String? = null,
            fechaFin: String? = null
        ): FirebaseResult<ReporteDashboardResponse> {
            val residents = readResidents()
            val consultations = readDocuments("consultas").map { it.toConsulta() }.inPeriod(fechaInicio, fechaFin) { it.fecha }
            val visits = readDocuments("visitas").map { it.toVisita() }.inPeriod(fechaInicio, fechaFin) { it.fecha }
            val payments = readDocuments("pagos").map { it.toPago() }.inPeriod(fechaInicio, fechaFin) { it.fecha_pago }
            val notifications = readDocuments("notificaciones").map { it.toNotificacion() }
            val activities = readDocuments("actividades")
            val activeResidents = residents.count { it.estado.equals("Activo", true) }
            val paid = payments.filter { it.estado.equals("Pagado", true) }
            val pending = payments.filterNot { it.estado.equals("Pagado", true) }
            val income = paid.sumOf { it.monto }
            val visitors = visits.sumOf { it.numero_visitantes }
            val report = ReporteDashboardResponse(
                success = true,
                fecha_actualizacion = nowString(),
                periodo = com.example.asilo360.data.model.PeriodoReporte(fechaInicio.orEmpty(), fechaFin.orEmpty()),
                resumen = ResumenKpi(
                    residentes = residents.size,
                    residentes_activos = activeResidents,
                    residentes_inactivos = residents.size - activeResidents,
                    personal = readDocuments("personal").size,
                    consultas = consultations.size,
                    visitas = visits.size,
                    ingresos = income,
                    pagos_pendientes_count = pending.size,
                    pagos_pendientes_monto = pending.sumOf { it.monto },
                    notificaciones_total = notifications.size,
                    notificaciones_leidas = notifications.count { it.leida },
                    notificaciones_no_leidas = notifications.count { !it.leida }
                ),
                residentes_data = ResidentesReporteData(
                    total = residents.size,
                    activos = activeResidents,
                    inactivos = residents.size - activeResidents,
                    por_mes = residents.groupBy { it.fecha_ingreso.orEmpty().take(7) }
                        .filterKeys { it.length == 7 }.map { MesCantidad(it.key, it.value.size) }.sortedBy { it.mes }
                ),
                consultas_data = com.example.asilo360.data.model.ConsultasReporteData(
                    total = consultations.size,
                    por_estado = consultations.groupingBy { it.estado }.eachCount().map { EstadoCantidad(it.key, it.value) },
                    por_mes = consultations.groupBy { it.fecha.take(7) }.filterKeys { it.length == 7 }
                        .map { MesCantidad(it.key, it.value.size) }.sortedBy { it.mes }
                ),
                visitas_data = com.example.asilo360.data.model.VisitasReporteData(
                    total = visits.size,
                    total_visitantes = visitors,
                    promedio_visitantes = if (visits.isEmpty()) 0.0 else visitors.toDouble() / visits.size,
                    por_estado = visits.groupingBy { it.estado }.eachCount().map { EstadoCantidad(it.key, it.value) },
                    por_mes = visits.groupBy { it.fecha.take(7) }.filterKeys { it.length == 7 }
                        .map { MesCantidad(it.key, it.value.size) }.sortedBy { it.mes }
                ),
                financiero_data = FinancieroReporteData(
                    ingresos_totales = income,
                    monto_pendiente = pending.sumOf { it.monto },
                    pagos_pendientes_count = pending.size,
                    ingresos_por_mes = paid.groupBy { it.fecha_pago.take(7) }.filterKeys { it.length == 7 }
                        .map { MesMonto(it.key, it.value.sumOf(Pago::monto)) }.sortedBy { it.mes },
                    pagos_por_metodo = paid.groupBy { it.metodo_pago }.map {
                        MetodoPagoResumen(it.key, it.value.size, it.value.sumOf(Pago::monto))
                    }
                ),
                actividad_reciente = activities.sortedByDescending { it.text("fecha") }.take(10).map {
                    com.example.asilo360.data.model.ActividadItem(
                        tipo_actividad = it.text("tipo"),
                        titulo = it.text("tipo").ifBlank { "Actividad" },
                        detalle = it.text("descripcion"),
                        fecha = it.text("fecha")
                    )
                }
            )
            return FirebaseResult(report)
        }
    }

    class ProfileService {
        suspend fun obtenerPerfil(idUsuario: Int): FirebaseResult<PerfilResponse> {
            val user = readUsers().firstOrNull { it.id_usuario == idUsuario }
                ?: throw IllegalStateException("No se encontró el perfil del usuario.")
            return FirebaseResult(PerfilResponse(success = true, usuario = user.asPerfil()))
        }

        suspend fun actualizarPerfil(request: ActualizarPerfilRequest): FirebaseResult<PerfilResponse> {
            val userDoc = findUserDocument(request.id_usuario)
            val currentUser = auth.currentUser
            if (currentUser?.email != request.correo && currentUser != null) {
                currentUser.updateEmail(request.correo).await()
            }
            userDoc.reference.set(
                mapOf(
                    "nombre" to request.nombre,
                    "apellido_paterno" to request.apellido_paterno,
                    "apellido_materno" to request.apellido_materno,
                    "correo" to request.correo,
                    "telefono" to request.telefono
                ),
                SetOptions.merge()
            ).await()
            return obtenerPerfil(request.id_usuario)
        }

        suspend fun cambiarPassword(
            idUsuario: Int,
            passwordActual: String,
            passwordNueva: String
        ): FirebaseResult<ConfiguracionOperationResponse> {
            val currentUser = auth.currentUser ?: throw IllegalStateException("No hay una sesión de Firebase activa.")
            require(readUsers().any { it.id_usuario == idUsuario }) { "No se encontró el perfil del usuario." }
            val correo = currentUser.email ?: throw IllegalStateException("La cuenta no tiene un correo asociado.")
            val credential = EmailAuthProvider.getCredential(correo, passwordActual)
            currentUser.reauthenticate(credential).await()
            currentUser.updatePassword(passwordNueva).await()
            return FirebaseResult(ConfiguracionOperationResponse(success = true, message = "Contraseña actualizada correctamente."))
        }

        suspend fun cambiarPassword(request: CambiarPasswordRequest): FirebaseResult<ConfiguracionOperationResponse> =
            cambiarPassword(request.id_usuario, request.password_actual, request.password_nueva)
    }

    private suspend fun readDocuments(collection: String): List<DocumentSnapshot> =
        db.collection(collection).get().await().documents.filterNot { it.id == "_counter" }

    private suspend fun readUsers(): List<Usuario> = readDocuments("usuarios").map { it.toUsuario() }

    private suspend fun readResidents(): List<Residente> = readDocuments("residentes").map { it.toResidente() }

    private suspend fun readStaff(): List<Personal> {
        val users = readUsers().associateBy { it.id_usuario }
        return readDocuments("personal").map { document ->
            val staff = document.toPersonal()
            val user = users[staff.id_usuario]
            staff.copy(
                nombre = user?.nombre.orEmpty(),
                apellido_paterno = user?.apellido_paterno.orEmpty(),
                apellido_materno = user?.apellido_materno,
                correo = user?.correo.orEmpty(),
                telefono = user?.telefono,
                password = null
            )
        }
    }

    private data class FamilyRecord(
        val id_familiar: Int,
        val id_usuario: Int,
        val parentesco: String,
        val telefono: String
    )

    private suspend fun readFamilies(): List<FamilyRecord> = readDocuments("familiares").map {
        FamilyRecord(it.int("id_familiar"), it.int("id_usuario"), it.text("parentesco"), it.text("telefono"))
    }

    private suspend fun insert(collectionName: String, idField: String, values: Map<String, Any?>): Int {
        val collection = db.collection(collectionName)
        val id = nextId(collectionName, idField)
        collection.document(id.toString()).set(values.withId(idField, id)).await()
        return id
    }

    private suspend fun nextId(collectionName: String, idField: String): Int {
        val collection = db.collection(collectionName)
        val existing = readDocuments(collectionName).mapNotNull { it.intOrNull(idField) }.maxOrNull() ?: 0
        val counter = collection.document("_counter")
        return db.runTransaction { transaction ->
            val current = transaction.get(counter).getLong("value")?.toInt() ?: existing
            val next = current + 1
            transaction.set(counter, mapOf("value" to next))
            next
        }.await()
    }

    private suspend fun update(collectionName: String, idField: String, id: Int, values: Map<String, Any?>) {
        val collection = db.collection(collectionName)
        val reference = collection.document(id.toString())
        val snapshot = reference.get().await()
        val target = if (snapshot.exists()) reference else collection.whereEqualTo(idField, id).get().await().documents.firstOrNull()?.reference
            ?: throw IllegalStateException("No se encontró el registro para actualizar.")
        target.set(values, SetOptions.merge()).await()
    }

    private suspend fun deleteRelated(collectionName: String, field: String, id: Int) {
        val documents = db.collection(collectionName).whereEqualTo(field, id).get().await().documents
        documents.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private suspend fun findUserDocument(idUsuario: Int): DocumentSnapshot {
        val documents = db.collection("usuarios").whereEqualTo("id_usuario", idUsuario).limit(1).get().await().documents
        return documents.firstOrNull() ?: throw IllegalStateException("No se encontró el perfil del usuario.")
    }

    private fun Map<String, Any?>.withId(field: String, id: Int): Map<String, Any?> = this + (field to id)

    private fun Residente.asMap(): Map<String, Any?> = mapOf(
        "nombre" to nombre, "apellido_paterno" to apellido_paterno, "apellido_materno" to apellido_materno,
        "fecha_nacimiento" to fecha_nacimiento, "sexo" to sexo, "habitacion" to habitacion,
        "fecha_ingreso" to fecha_ingreso, "estado" to estado, "observaciones" to observaciones
    )

    private fun Personal.asMap(): Map<String, Any?> = mapOf(
        "id_usuario" to id_usuario, "puesto" to puesto, "fecha_ingreso" to fecha_ingreso, "estado" to estado
    )

    private fun Consulta.asMap(): Map<String, Any?> = mapOf(
        "id_residente" to id_residente, "id_personal" to id_personal, "fecha" to fecha, "hora" to hora,
        "motivo" to motivo, "observaciones" to observaciones, "estado" to estado
    )

    private fun com.example.asilo360.data.model.Actividad.asMap(): Map<String, Any?> = mapOf(
        "id_residente" to id_residente, "id_personal" to id_personal, "fecha" to fecha, "hora" to hora,
        "tipo" to tipo, "descripcion" to descripcion
    )

    private fun Visita.asMap(): Map<String, Any?> = mapOf(
        "id_residente" to id_residente, "id_familiar" to id_familiar, "fecha" to fecha, "hora" to hora,
        "numero_visitantes" to numero_visitantes, "estado" to estado
    )

    private fun Pago.asMap(): Map<String, Any?> = mapOf(
        "id_residente" to id_residente, "monto" to monto, "fecha_pago" to fecha_pago,
        "concepto" to concepto, "metodo_pago" to metodo_pago, "estado" to estado
    )

    private fun DocumentSnapshot.toUsuario(): Usuario {
        val idRol = int("id_rol")
        return Usuario(
            id_usuario = int("id_usuario", id.toIntOrNull() ?: 0), nombre = text("nombre"),
            apellido_paterno = text("apellido_paterno"), apellido_materno = text("apellido_materno").ifBlank { null },
            correo = text("correo"), id_rol = idRol,
            rol = text("rol").ifBlank { text("nombre_rol").ifBlank { when (idRol) { 1 -> "Administrador"; 2 -> "Personal"; 3 -> "Familiar"; else -> "" } } },
            telefono = text("telefono").ifBlank { null }
        )
    }

    private fun DocumentSnapshot.toResidente() = Residente(
        id_residente = int("id_residente", id.toIntOrNull() ?: 0), nombre = text("nombre"), apellido_paterno = text("apellido_paterno"),
        apellido_materno = text("apellido_materno").ifBlank { null }, fecha_nacimiento = text("fecha_nacimiento").ifBlank { null },
        sexo = text("sexo").ifBlank { null }, habitacion = text("habitacion").ifBlank { null },
        fecha_ingreso = text("fecha_ingreso").ifBlank { null }, estado = text("estado", "Activo"),
        observaciones = text("observaciones").ifBlank { null }
    )

    private fun DocumentSnapshot.toPersonal() = Personal(
        id_personal = int("id_personal", id.toIntOrNull() ?: 0), id_usuario = int("id_usuario"), nombre = text("nombre"),
        apellido_paterno = text("apellido_paterno"), apellido_materno = text("apellido_materno").ifBlank { null },
        correo = text("correo"), password = null, telefono = text("telefono").ifBlank { null },
        puesto = text("puesto"), fecha_ingreso = text("fecha_ingreso").ifBlank { null }, estado = int("estado", 1)
    )

    private fun DocumentSnapshot.toConsulta() = Consulta(
        id_consulta = int("id_consulta", id.toIntOrNull() ?: 0), id_residente = int("id_residente"), id_personal = int("id_personal"),
        fecha = text("fecha"), hora = text("hora"), motivo = text("motivo"),
        observaciones = text("observaciones").ifBlank { null }, estado = text("estado", "Programada")
    )

    private fun DocumentSnapshot.toActividad() = com.example.asilo360.data.model.Actividad(
        id_actividad = int("id_actividad", id.toIntOrNull() ?: 0), id_residente = int("id_residente"), id_personal = int("id_personal"),
        fecha = text("fecha"), hora = text("hora"), tipo = text("tipo"), descripcion = text("descripcion")
    )

    private fun DocumentSnapshot.toVisita() = Visita(
        id_visita = int("id_visita", id.toIntOrNull() ?: 0),
        id_residente = int("id_residente"),
        id_familiar = int("id_familiar"),
        fecha = text("fecha"),
        hora = text("hora"),
        numero_visitantes = int("numero_visitantes", 1),
        estado = text("estado", "Programada"),
        observaciones = text("observaciones").ifBlank { null },
        fecha_creacion = text("fecha_creacion").ifBlank { null },
        nombre_residente = text("nombre_residente").ifBlank { text("residente").ifBlank { null } },
        habitacion = text("habitacion").ifBlank { null },
        nombre_familiar = text("nombre_familiar").ifBlank { text("familiar").ifBlank { null } },
        parentesco = text("parentesco").ifBlank { null }
    )

    private fun DocumentSnapshot.toPago() = Pago(
        id_pago = int("id_pago", id.toIntOrNull() ?: 0), id_residente = int("id_residente"), monto = double("monto"),
        fecha_pago = text("fecha_pago"), concepto = text("concepto"), metodo_pago = text("metodo_pago"),
        estado = text("estado", "Pagado")
    )

    private fun DocumentSnapshot.toNotificacion() = Notificacion(
        id_notificacion = int("id_notificacion", id.toIntOrNull() ?: 0), id_usuario = int("id_usuario"),
        titulo = text("titulo"), mensaje = text("mensaje"), tipo = text("tipo", "General"),
        leida = boolean("leida"), fecha = text("fecha").ifBlank { null }
    )

    private fun PerfilUsuario.asMap(): Map<String, Any?> = mapOf(
        "id_usuario" to id_usuario, "nombre" to nombre, "apellido_paterno" to apellido_paterno,
        "apellido_materno" to apellido_materno, "correo" to correo, "telefono" to telefono,
        "id_rol" to id_rol, "rol" to rol
    )

    private fun Usuario.asPerfil() = PerfilUsuario(
        id_usuario = id_usuario, nombre = nombre, apellido_paterno = apellido_paterno.orEmpty(),
        apellido_materno = apellido_materno.orEmpty(), correo = correo, telefono = telefono.orEmpty(), id_rol = id_rol, rol = rol
    )

    private fun Usuario.nombreCompleto(): String = "$nombre ${apellido_paterno.orEmpty()}".trim()
    private fun Residente.nombreCompleto(): String = "$nombre $apellido_paterno ${apellido_materno.orEmpty()}".trim()
    private fun Personal.nombreCompleto(): String = "$nombre $apellido_paterno ${apellido_materno.orEmpty()}".trim()

    private fun DocumentSnapshot.text(field: String, default: String = ""): String = when (val value = get(field)) {
        is String -> value
        is Number -> value.toString()
        is Boolean -> if (value) "1" else "0"
        is Date -> SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(value)
        else -> default
    }

    private fun DocumentSnapshot.int(field: String, default: Int = 0): Int = intOrNull(field) ?: default

    private fun DocumentSnapshot.intOrNull(field: String): Int? = when (val value = get(field)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> id.toIntOrNull()
    }

    private fun DocumentSnapshot.double(field: String): Double = when (val value = get(field)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun DocumentSnapshot.boolean(field: String): Boolean = when (val value = get(field)) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.equals("true", true) || value == "1"
        else -> false
    }

    private fun <T> List<T>.inPeriod(start: String?, end: String?, date: (T) -> String): List<T> = filter {
        val value = date(it).take(10)
        (start.isNullOrBlank() || value >= start) && (end.isNullOrBlank() || value <= end)
    }

    private fun nowString(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Date())

    private const val STAFF_AUTH_APP = "Asilo360StaffRegistration"
}