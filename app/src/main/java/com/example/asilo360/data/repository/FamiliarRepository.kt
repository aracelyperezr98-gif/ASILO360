package com.example.asilo360.data.repository

import android.util.Log
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.Pago
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.data.model.Visita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio encargado de resolver los residentes vinculados a un usuario con rol Familiar
 * directamente desde Cloud Firestore, garantizando seguridad, filtrado por permisos y
 * resumen de seguimiento de eventos en tiempo real.
 */
class FamiliarRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Obtiene la lista de residentes vinculados al usuario familiar actual.
     *
     * @param idUsuario ID numérico del usuario (`id_usuario`). Si es <= 0, intenta resolverlo desde Firebase Auth.
     */
    suspend fun obtenerResidentesVinculados(idUsuario: Int): List<ResidenteVinculado> {
        val targetUserId = if (idUsuario > 0) idUsuario else resolverIdUsuarioActual()
        if (targetUserId <= 0) {
            Log.w(TAG, "No se pudo identificar un id_usuario válido.")
            return emptyList()
        }

        // 1. Obtener registros de vinculación en 'residente_familiar' por id_usuario o id_familiar
        val relDocsByUser = firestore.collection("residente_familiar")
            .whereEqualTo("id_usuario", targetUserId)
            .get()
            .await()
            .documents

        val familiarDocs = firestore.collection("familiares")
            .whereEqualTo("id_usuario", targetUserId)
            .get()
            .await()
            .documents

        val familiarIds = familiarDocs.mapNotNull { it.leerEntero("id_familiar") }.toSet()

        val relDocsByFam = mutableListOf<DocumentSnapshot>()
        for (famId in familiarIds) {
            val docs = firestore.collection("residente_familiar")
                .whereEqualTo("id_familiar", famId)
                .get()
                .await()
                .documents
            relDocsByFam.addAll(docs)
        }

        val todosLosRelDocs = (relDocsByUser + relDocsByFam).distinctBy { it.id }

        val relacionesList = todosLosRelDocs.mapNotNull { rel ->
            val idRes = rel.leerEntero("id_residente") ?: return@mapNotNull null
            val idFam = rel.leerEntero("id_familiar") ?: targetUserId
            val parentesco = rel.getString("parentesco")?.takeIf { it.isNotBlank() } ?: "Familiar"
            Triple(idRes, idFam, parentesco)
        }

        if (relacionesList.isEmpty()) {
            Log.i(TAG, "El usuario $targetUserId no tiene residentes vinculados en Firestore.")
            return emptyList()
        }

        // Eliminamos duplicados por id_residente conservando la primera relación encontrada
        val relacionesUnicas = relacionesList.distinctBy { it.first }

        // 2. Cargar la información oficial de cada residente y su resumen de eventos
        val resultado = mutableListOf<ResidenteVinculado>()
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

        for ((idRes, idFam, parentesco) in relacionesUnicas) {
            val residenteDoc = obtenerDocResidente(idRes) ?: continue
            val residente = residenteDoc.toResidente()

            // Cargar resumen de seguimiento para este residente
            val consultas = obtenerConsultasDeResidente(idRes)
            val proximaConsulta = consultas
                .filter { it.estado != "Cancelada" && it.fecha >= hoy }
                .minByOrNull { it.fecha }
                ?: consultas.firstOrNull()

            val visitas = obtenerVisitasDeResidente(idRes, idFam)
            val proximaVisita = visitas
                .filter { it.estado != "Cancelada" && it.fecha >= hoy }
                .minByOrNull { it.fecha }
                ?: visitas.firstOrNull()

            val actividadesCount = contarActividadesDeResidente(idRes)

            val pagos = obtenerPagosDeResidente(idRes)
            val pagosPendientesCount = pagos.count { !it.estado.equals("Pagado", ignoreCase = true) }
            val ultimoPago = pagos.maxByOrNull { it.fecha_pago }

            resultado.add(
                ResidenteVinculado(
                    residente = residente,
                    parentesco = parentesco,
                    idFamiliar = idFam,
                    totalConsultas = consultas.size,
                    proximaConsulta = proximaConsulta,
                    totalVisitas = visitas.size,
                    proximaVisita = proximaVisita,
                    totalActividades = actividadesCount,
                    pagosPendientesCount = pagosPendientesCount,
                    ultimoPago = ultimoPago
                )
            )
        }

        return resultado
    }

    private suspend fun resolverIdUsuarioActual(): Int {
        val currentUser = auth.currentUser ?: return 0
        val email = currentUser.email.orEmpty()
        val uid = currentUser.uid

        // Intentar buscar el usuario por UID en la colección 'usuarios'
        val docUid = firestore.collection("usuarios").document(uid).get().await()
        if (docUid.exists()) {
            docUid.leerEntero("id_usuario")?.let { return it }
        }

        // Buscar por correo electrónico
        if (email.isNotBlank()) {
            val docs = firestore.collection("usuarios")
                .whereEqualTo("correo", email)
                .limit(1)
                .get()
                .await()
                .documents

            docs.firstOrNull()?.leerEntero("id_usuario")?.let { return it }
        }

        return 0
    }

    private suspend fun obtenerDocResidente(idResidente: Int): DocumentSnapshot? {
        val docDirecto = firestore.collection("residentes").document(idResidente.toString()).get().await()
        if (docDirecto.exists()) return docDirecto

        val queryDocs = firestore.collection("residentes")
            .whereEqualTo("id_residente", idResidente)
            .limit(1)
            .get()
            .await()
            .documents

        return queryDocs.firstOrNull()
    }

    private suspend fun obtenerConsultasDeResidente(idResidente: Int): List<Consulta> {
        return try {
            firestore.collection("consultas")
                .whereEqualTo("id_residente", idResidente)
                .get()
                .await()
                .documents
                .map { it.toConsulta() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun obtenerVisitasDeResidente(idResidente: Int, idFamiliar: Int): List<Visita> {
        return try {
            firestore.collection("visitas")
                .whereEqualTo("id_residente", idResidente)
                .get()
                .await()
                .documents
                .map { it.toVisita() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun contarActividadesDeResidente(idResidente: Int): Int {
        return try {
            firestore.collection("actividades")
                .whereEqualTo("id_residente", idResidente)
                .get()
                .await()
                .documents.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun obtenerPagosDeResidente(idResidente: Int): List<Pago> {
        return try {
            firestore.collection("pagos")
                .whereEqualTo("id_residente", idResidente)
                .get()
                .await()
                .documents
                .map { it.toPago() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Helpers de Mapeo de DocumentSnapshot ---

    private fun DocumentSnapshot.leerEntero(campo: String): Int? {
        return getLong(campo)?.toInt() ?: getString(campo)?.toIntOrNull()
    }

    private fun DocumentSnapshot.toResidente(): Residente {
        val idVal = leerEntero("id_residente") ?: id.toIntOrNull() ?: 0
        return Residente(
            id_residente = idVal,
            nombre = getString("nombre") ?: "",
            apellido_paterno = getString("apellido_paterno") ?: "",
            apellido_materno = getString("apellido_materno")?.takeIf { it.isNotBlank() },
            fecha_nacimiento = getString("fecha_nacimiento")?.takeIf { it.isNotBlank() },
            sexo = getString("sexo")?.takeIf { it.isNotBlank() },
            habitacion = getString("habitacion")?.takeIf { it.isNotBlank() },
            fecha_ingreso = getString("fecha_ingreso")?.takeIf { it.isNotBlank() },
            estado = getString("estado") ?: "Activo",
            observaciones = getString("observaciones")?.takeIf { it.isNotBlank() }
        )
    }

    private fun DocumentSnapshot.toConsulta(): Consulta {
        val idVal = leerEntero("id_consulta") ?: id.toIntOrNull() ?: 0
        return Consulta(
            id_consulta = idVal,
            id_residente = leerEntero("id_residente") ?: 0,
            id_personal = leerEntero("id_personal") ?: 0,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            motivo = getString("motivo").orEmpty(),
            observaciones = getString("observaciones")?.takeIf { it.isNotBlank() },
            estado = getString("estado") ?: "Programada"
        )
    }

    private fun DocumentSnapshot.toVisita(): Visita {
        val idVal = leerEntero("id_visita") ?: id.toIntOrNull() ?: 0
        return Visita(
            id_visita = idVal,
            id_residente = leerEntero("id_residente") ?: 0,
            id_familiar = leerEntero("id_familiar") ?: 0,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            numero_visitantes = leerEntero("numero_visitantes") ?: 1,
            estado = getString("estado") ?: "Programada",
            observaciones = getString("observaciones")
        )
    }

    private fun DocumentSnapshot.toPago(): Pago {
        val idVal = leerEntero("id_pago") ?: id.toIntOrNull() ?: 0
        val montoVal = getDouble("monto") ?: getString("monto")?.toDoubleOrNull() ?: 0.0
        return Pago(
            id_pago = idVal,
            id_residente = leerEntero("id_residente") ?: 0,
            monto = montoVal,
            fecha_pago = getString("fecha_pago").orEmpty(),
            concepto = getString("concepto").orEmpty(),
            metodo_pago = getString("metodo_pago").orEmpty(),
            estado = getString("estado") ?: "Pagado"
        )
    }

    companion object {
        private const val TAG = "FamiliarRepository"
    }
}
