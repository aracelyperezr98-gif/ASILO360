package com.example.asilo360.data.repository

import android.util.Log
import com.example.asilo360.data.firebase.FirebaseClient
import com.example.asilo360.data.model.ResidenteVinculado
import com.example.asilo360.data.model.Visita
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio enfocado en la gestión y consulta segura de visitas programadas
 * para el rol Familiar.
 */
class FamiliarVisitasRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val familiarRepository: FamiliarRepository = FamiliarRepository()
) {

    /**
     * Obtiene la lista de residentes autorizados para la cuenta familiar actual.
     */
    suspend fun obtenerResidentesAutorizados(idUsuario: Int): List<ResidenteVinculado> {
        return familiarRepository.obtenerResidentesVinculados(idUsuario)
    }

    /**
     * Obtiene las visitas únicamente de los residentes vinculados al familiar autenticado.
     *
     * @param idUsuario ID del usuario autenticado.
     * @param idResidenteFiltro ID opcional para filtrar por un residente específico.
     */
    suspend fun obtenerVisitasAutorizadas(
        idUsuario: Int,
        idResidenteFiltro: Int? = null
    ): List<Visita> {
        val residentesVinculados = familiarRepository.obtenerResidentesVinculados(idUsuario)
        if (residentesVinculados.isEmpty()) {
            Log.i(TAG, "El usuario $idUsuario no tiene residentes vinculados. No se cargaron visitas.")
            return emptyList()
        }

        val idsAutorizados = if (idResidenteFiltro != null && idResidenteFiltro > 0) {
            residentesVinculados.map { it.residente.id_residente }.filter { it == idResidenteFiltro }
        } else {
            residentesVinculados.map { it.residente.id_residente }
        }

        if (idsAutorizados.isEmpty()) {
            Log.w(TAG, "El residente especificado ($idResidenteFiltro) no está autorizado para este familiar.")
            return emptyList()
        }

        val mapaResidentes = residentesVinculados.associateBy { it.residente.id_residente }
        val resultadoVisitas = mutableListOf<Visita>()

        for (idRes in idsAutorizados) {
            try {
                val docs = firestore.collection("visitas")
                    .whereEqualTo("id_residente", idRes)
                    .get()
                    .await()
                    .documents

                val vinculado = mapaResidentes[idRes]
                val nombreRes = vinculado?.residente?.let {
                    "${it.nombre} ${it.apellido_paterno}".trim()
                } ?: "Residente"
                val habitacion = vinculado?.residente?.habitacion
                val idFam = vinculado?.idFamiliar ?: idUsuario
                val parentesco = vinculado?.parentesco ?: "Familiar"

                for (doc in docs) {
                    val visitaBase = doc.toVisitaSegura()
                    resultadoVisitas.add(
                        visitaBase.copy(
                            id_familiar = if (visitaBase.id_familiar > 0) visitaBase.id_familiar else idFam,
                            nombre_residente = nombreRes,
                            habitacion = habitacion,
                            parentesco = visitaBase.parentesco?.takeIf { it.isNotBlank() } ?: parentesco
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al consultar visitas del residente $idRes", e)
            }
        }

        return resultadoVisitas
    }

    /**
     * Consulta el detalle de una visita verificando autorización de lectura.
     */
    suspend fun obtenerDetalleVisitaAutorizada(idUsuario: Int, idVisita: Int): Visita? {
        val visitas = obtenerVisitasAutorizadas(idUsuario)
        return visitas.find { it.id_visita == idVisita }
    }

    /**
     * Registra una nueva solicitud de visita en Firestore con estado inicial 'Programada'.
     */
    suspend fun solicitarVisita(visita: Visita): Boolean {
        return try {
            val response = FirebaseClient.visitasApiService.crearVisita(visita)
            response.body().success
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar la solicitud de visita", e)
            false
        }
    }

    private fun DocumentSnapshot.leerEntero(campo: String): Int? {
        return getLong(campo)?.toInt() ?: getString(campo)?.toIntOrNull()
    }

    private fun DocumentSnapshot.toVisitaSegura(): Visita {
        val idVal = leerEntero("id_visita") ?: id.toIntOrNull() ?: 0
        return Visita(
            id_visita = idVal,
            id_residente = leerEntero("id_residente") ?: 0,
            id_familiar = leerEntero("id_familiar") ?: 0,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            numero_visitantes = leerEntero("numero_visitantes") ?: 1,
            estado = getString("estado") ?: "Programada",
            observaciones = getString("observaciones")?.takeIf { it.isNotBlank() },
            fecha_creacion = getString("fecha_creacion"),
            nombre_residente = getString("nombre_residente") ?: getString("residente"),
            habitacion = getString("habitacion"),
            nombre_familiar = getString("nombre_familiar") ?: getString("familiar"),
            parentesco = getString("parentesco")
        )
    }

    companion object {
        private const val TAG = "FamiliarVisitasRepo"
    }
}
