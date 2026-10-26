package com.example.asilo360.data.repository

import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Notificacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para la gestión de Notificaciones del Personal en Cloud Firestore.
 */
class NotificacionesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notificacionesCollection = firestore.collection("notificaciones")

    /**
     * Escucha en tiempo real las notificaciones del usuario autenticado en Firestore.
     */
    fun obtenerNotificacionesEnTiempoReal(idUsuario: Int): Flow<List<Notificacion>> = callbackFlow {
        val listener: ListenerRegistration = notificacionesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toNotificacionSegura()
                        } catch (_: Exception) {
                            null
                        }
                    }.filter { n ->
                        n.id_usuario == idUsuario || n.id_usuario == 0 || n.rol.equals("Personal", true) || n.rol.equals("Todos", true)
                    }.sortedByDescending { it.fecha.orEmpty() }

                    trySend(lista)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Escucha en tiempo real las notificaciones destinadas al usuario con rol Familiar.
     */
    fun obtenerNotificacionesFamiliarEnTiempoReal(idUsuario: Int): Flow<List<Notificacion>> = callbackFlow {
        val listener: ListenerRegistration = notificacionesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toNotificacionSegura()
                        } catch (_: Exception) {
                            null
                        }
                    }.filter { n ->
                        n.id_usuario == idUsuario || n.id_usuario == 0 || n.rol.equals("Familiar", true) || n.rol.equals("Todos", true)
                    }.sortedByDescending { it.fecha.orEmpty() }

                    trySend(lista)
                }
            }

        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toNotificacionSegura(): Notificacion {
        val idVal = getLong("id_notificacion")?.toInt() ?: id.toIntOrNull() ?: 0
        val idUsr = getLong("id_usuario")?.toInt() ?: getString("id_usuario")?.toIntOrNull() ?: 0
        val idRef = getLong("id_referencia")?.toInt() ?: getString("id_referencia")?.toIntOrNull()

        return Notificacion(
            id_notificacion = idVal,
            id_usuario = idUsr,
            destinatario = getString("destinatario"),
            rol = getString("rol"),
            titulo = getString("titulo") ?: "Notificación",
            mensaje = getString("mensaje") ?: "",
            tipo = getString("tipo") ?: "General",
            leida = getBoolean("leida") ?: false,
            fecha = getString("fecha"),
            id_referencia = idRef,
            tipo_referencia = getString("tipo_referencia")
        )
    }

    /**
     * Obtener notificaciones del usuario vía FirebaseClient.
     */
    suspend fun obtenerNotificaciones(idUsuario: Int): List<Notificacion> {
        val result = FirebaseClient.notificacionesApiService.listarNotificaciones()
        val todas = result.body().notificaciones ?: emptyList()
        return todas.filter { n ->
            n.id_usuario == idUsuario || n.id_usuario == 0 || n.rol.equals("Personal", true)
        }.sortedByDescending { it.fecha.orEmpty() }
    }

    /**
     * Obtener una notificación por ID.
     */
    suspend fun obtenerNotificacionPorId(idNotificacion: Int, idUsuario: Int): Notificacion? {
        val todas = obtenerNotificaciones(idUsuario)
        return todas.find { it.id_notificacion == idNotificacion }
    }

    /**
     * Marcar una notificación como leída en Cloud Firestore.
     */
    suspend fun marcarComoLeida(idNotificacion: Int) {
        val updates = mapOf<String, Any?>("leida" to true)
        val docRef = notificacionesCollection.document(idNotificacion.toString())
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            docRef.update(updates).await()
        } else {
            val docs = notificacionesCollection.whereEqualTo("id_notificacion", idNotificacion).get().await()
            docs.documents.firstOrNull()?.reference?.update(updates)?.await()
        }
    }

    /**
     * Marcar todas las notificaciones del usuario como leídas.
     */
    suspend fun marcarTodasComoLeidas(idUsuario: Int) {
        val docs = notificacionesCollection.get().await().documents
        val targetDocs = docs.filter { doc ->
            val uId = doc.getLong("id_usuario")?.toInt() ?: 0
            val leida = doc.getBoolean("leida") ?: false
            (uId == idUsuario || uId == 0) && !leida
        }

        targetDocs.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { doc ->
                batch.update(doc.reference, "leida", true)
            }
            batch.commit().await()
        }
    }
}
