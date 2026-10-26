package com.example.asilo360.data.repository

import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.Pago
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repositorio centralizado para el manejo de Pagos en Cloud Firestore.
 */
class PagosRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val pagosCollection = firestore.collection("pagos")

    /**
     * Escucha en tiempo real los cambios en la colección 'pagos'.
     */
    fun obtenerPagosEnTiempoReal(): Flow<List<Pago>> = callbackFlow {
        val listener: ListenerRegistration = pagosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val lista = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toPagoSeguro()
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(lista)
            }
        }

        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toPagoSeguro(): Pago {
        val idVal = getLong("id_pago")?.toInt() ?: id.toIntOrNull() ?: 0
        val idRes = getLong("id_residente")?.toInt() ?: getString("id_residente")?.toIntOrNull() ?: 0
        val mnt = getDouble("monto") ?: getLong("monto")?.toDouble() ?: 0.0

        return Pago(
            id_pago = idVal,
            id_residente = idRes,
            residente = getString("residente"),
            monto = mnt,
            fecha_pago = getString("fecha_pago").orEmpty(),
            concepto = getString("concepto") ?: "Mensualidad",
            metodo_pago = getString("metodo_pago") ?: "Transferencia",
            estado = getString("estado") ?: "Pagado"
        )
    }

    suspend fun obtenerPagos(): List<Pago> {
        val res = FirebaseClient.pagosApiService.listarPagos()
        return res.body().pagos ?: emptyList()
    }

    suspend fun obtenerPagoPorId(idPago: Int): Pago? {
        val todas = obtenerPagos()
        return todas.find { it.id_pago == idPago }
    }
}
