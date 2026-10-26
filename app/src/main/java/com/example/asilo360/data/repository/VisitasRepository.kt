package com.example.asilo360.data.repository

import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Visita
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio centralizado para el manejo de Visitas Programadas en Cloud Firestore.
 */
class VisitasRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val visitasCollection = firestore.collection("visitas")

    /**
     * Escucha en tiempo real los cambios en la colección 'visitas'.
     */
    fun obtenerVisitasEnTiempoReal(): Flow<List<Visita>> = callbackFlow {
        val listener: ListenerRegistration = visitasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val listaDirecta = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toVisitaSegura()
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(listaDirecta)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Mapeo seguro de DocumentSnapshot a Visita
     */
    private fun DocumentSnapshot.toVisitaSegura(): Visita {
        val idVal = getLong("id_visita")?.toInt() ?: id.toIntOrNull() ?: 0
        val idRes = getLong("id_residente")?.toInt() ?: getString("id_residente")?.toIntOrNull() ?: 0
        val idFam = getLong("id_familiar")?.toInt() ?: getString("id_familiar")?.toIntOrNull() ?: 0
        val numVis = getLong("numero_visitantes")?.toInt() ?: getString("numero_visitantes")?.toIntOrNull() ?: 1

        return Visita(
            id_visita = idVal,
            id_residente = idRes,
            id_familiar = idFam,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            numero_visitantes = numVis,
            estado = getString("estado") ?: "Programada",
            observaciones = getString("observaciones"),
            fecha_creacion = getString("fecha_creacion"),
            nombre_residente = getString("nombre_residente") ?: getString("residente"),
            habitacion = getString("habitacion"),
            nombre_familiar = getString("nombre_familiar") ?: getString("familiar"),
            parentesco = getString("parentesco")
        )
    }

    /**
     * Obtiene todas las visitas mediante FirebaseClient (con joins a Residentes, Usuarios y Familiares).
     */
    suspend fun obtenerVisitas(): List<Visita> {
        val result = FirebaseClient.visitasApiService.listarVisitas()
        return result.body().visitas ?: emptyList()
    }

    /**
     * Obtiene el detalle de una visita por ID.
     */
    suspend fun obtenerVisitaPorId(idVisita: Int): Visita? {
        val todas = obtenerVisitas()
        return todas.find { it.id_visita == idVisita }
    }

    /**
     * Actualiza el estado de la visita (Programada, Confirmada, Realizada, Cancelada) en Cloud Firestore.
     */
    suspend fun actualizarEstado(idVisita: Int, nuevoEstado: String, observaciones: String? = null) {
        val updates = mutableMapOf<String, Any?>("estado" to nuevoEstado)
        if (observaciones != null) {
            updates["observaciones"] = observaciones
        }
        val docRef = visitasCollection.document(idVisita.toString())
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            docRef.update(updates).await()
        } else {
            val docs = visitasCollection.whereEqualTo("id_visita", idVisita).get().await()
            docs.documents.firstOrNull()?.reference?.update(updates)?.await()
        }
    }

    /**
     * Actualiza las observaciones de la visita en Cloud Firestore.
     */
    suspend fun actualizarObservaciones(idVisita: Int, observaciones: String) {
        val updates = mapOf<String, Any?>("observaciones" to observaciones)
        val docRef = visitasCollection.document(idVisita.toString())
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            docRef.update(updates).await()
        } else {
            val docs = visitasCollection.whereEqualTo("id_visita", idVisita).get().await()
            docs.documents.firstOrNull()?.reference?.update(updates)?.await()
        }
    }
}
