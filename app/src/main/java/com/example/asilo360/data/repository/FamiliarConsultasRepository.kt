package com.example.asilo360.data.repository

import android.util.Log
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.ResidenteVinculado
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de seguridad para el manejo de consultas médicas del rol Familiar.
 * Garantiza que únicamente se consulten citas médicas correspondientes a los residentes
 * explícitamente vinculados y autorizados para la cuenta autenticada.
 */
class FamiliarConsultasRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val familiarRepository: FamiliarRepository = FamiliarRepository()
) {

    /**
     * Obtiene los residentes autorizados para el usuario familiar actual.
     */
    suspend fun obtenerResidentesAutorizados(idUsuario: Int): List<ResidenteVinculado> {
        return familiarRepository.obtenerResidentesVinculados(idUsuario)
    }

    /**
     * Obtiene las consultas médicas únicamente de los residentes vinculados al usuario.
     *
     * @param idUsuario ID del usuario autenticado.
     * @param idResidenteFiltro ID opcional del residente a filtrar. Si es null o <= 0, consulta todos sus familiares.
     */
    suspend fun obtenerConsultasAutorizadas(
        idUsuario: Int,
        idResidenteFiltro: Int? = null
    ): List<Consulta> {
        val residentesVinculados = familiarRepository.obtenerResidentesVinculados(idUsuario)
        if (residentesVinculados.isEmpty()) {
            Log.i(TAG, "El usuario $idUsuario no tiene residentes vinculados. No se consultarán citas.")
            return emptyList()
        }

        // Determinar qué residentes están autorizados
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
        val mapaPersonal = cargarMapaPersonal()

        val resultadoConsultas = mutableListOf<Consulta>()

        for (idRes in idsAutorizados) {
            try {
                val docs = firestore.collection("consultas")
                    .whereEqualTo("id_residente", idRes)
                    .get()
                    .await()
                    .documents

                val vinculado = mapaResidentes[idRes]
                val nombreRes = vinculado?.residente?.let {
                    "${it.nombre} ${it.apellido_paterno}".trim()
                } ?: "Residente"
                val habitacion = vinculado?.residente?.habitacion

                for (doc in docs) {
                    val consultaBase = doc.toConsultaSegura()
                    val idStaff = consultaBase.id_personal
                    val staffInfo = mapaPersonal[idStaff]

                    resultadoConsultas.add(
                        consultaBase.copy(
                            nombre_residente = nombreRes,
                            residente = nombreRes,
                            habitacion = habitacion,
                            nombre_personal = staffInfo?.first ?: consultaBase.nombre_personal ?: "Personal Médico",
                            personal = staffInfo?.first ?: consultaBase.personal ?: "Personal Médico",
                            puesto_personal = staffInfo?.second ?: consultaBase.puesto_personal ?: "Enfermería / Medicina"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al consultar las citas del residente $idRes", e)
            }
        }

        return resultadoConsultas
    }

    /**
     * Obtiene el detalle de una consulta por ID, verificando que pertenezca a un residente vinculado al familiar.
     */
    suspend fun obtenerDetalleConsultaAutorizada(idUsuario: Int, idConsulta: Int): Consulta? {
        val consultas = obtenerConsultasAutorizadas(idUsuario)
        return consultas.find { it.id_consulta == idConsulta }
    }

    /**
     * Carga un mapa auxiliar id_personal -> Pair(nombreCompleto, puesto) desde Firestore.
     */
    private suspend fun cargarMapaPersonal(): Map<Int, Pair<String, String>> {
        return try {
            val usuariosMap = firestore.collection("usuarios")
                .get()
                .await()
                .documents
                .associate { doc ->
                    val idU = doc.leerEntero("id_usuario") ?: 0
                    val nom = doc.getString("nombre") ?: ""
                    val pat = doc.getString("apellido_paterno") ?: ""
                    idU to "$nom $pat".trim()
                }

            val personalDocs = firestore.collection("personal")
                .get()
                .await()
                .documents

            val mapa = mutableMapOf<Int, Pair<String, String>>()
            for (pDoc in personalDocs) {
                val idP = pDoc.leerEntero("id_personal") ?: pDoc.id.toIntOrNull() ?: continue
                val idU = pDoc.leerEntero("id_usuario") ?: 0
                val puesto = pDoc.getString("puesto") ?: "Personal de Salud"
                val nombre = usuariosMap[idU]?.takeIf { it.isNotBlank() } ?: "Personal de Salud"
                mapa[idP] = Pair(nombre, puesto)
            }
            mapa
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun DocumentSnapshot.leerEntero(campo: String): Int? {
        return getLong(campo)?.toInt() ?: getString(campo)?.toIntOrNull()
    }

    private fun DocumentSnapshot.toConsultaSegura(): Consulta {
        val idVal = leerEntero("id_consulta") ?: id.toIntOrNull() ?: 0
        return Consulta(
            id_consulta = idVal,
            id_residente = leerEntero("id_residente") ?: 0,
            id_personal = leerEntero("id_personal") ?: 0,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            motivo = getString("motivo") ?: "Consulta médica general",
            observaciones = getString("observaciones")?.takeIf { it.isNotBlank() },
            estado = getString("estado") ?: "Programada",
            nombre_residente = getString("nombre_residente") ?: getString("residente"),
            nombre_personal = getString("nombre_personal") ?: getString("personal"),
            puesto_personal = getString("puesto_personal")
        )
    }

    companion object {
        private const val TAG = "FamiliarConsultasRepository"
    }
}
