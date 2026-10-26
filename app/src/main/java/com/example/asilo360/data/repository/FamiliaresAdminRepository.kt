package com.example.asilo360.data.repository

import android.util.Log
import com.example.asilo360.data.model.Residente
import com.example.asilo360.data.model.ResidenteFamiliarVinculo
import com.example.asilo360.data.model.Usuario
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio administrativo para gestionar cuentas de familiares y sus vínculos con los residentes en Firestore.
 */
class FamiliaresAdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Obtiene la lista de todos los usuarios registrados con el rol de Familiar (id_rol = 3).
     */
    suspend fun obtenerUsuariosFamiliares(): List<Usuario> {
        val docs = firestore.collection("usuarios")
            .whereEqualTo("id_rol", 3)
            .get()
            .await()
            .documents

        return docs.map { it.toUsuario() }
    }

    /**
     * Obtiene todos los residentes registrados.
     */
    suspend fun obtenerResidentes(): List<Residente> {
        val docs = firestore.collection("residentes")
            .get()
            .await()
            .documents
            .filterNot { it.id == "_counter" }

        return docs.map { it.toResidente() }
    }

    /**
     * Obtiene todos los vínculos de la colección 'residente_familiar'.
     */
    suspend fun obtenerTodosLosVinculos(): List<ResidenteFamiliarVinculo> {
        val vinculoDocs = firestore.collection("residente_familiar")
            .get()
            .await()
            .documents

        val residentes = obtenerResidentes().associateBy { it.id_residente }
        val usuariosFamiliares = obtenerUsuariosFamiliares().associateBy { it.id_usuario }
        val familiaresDocs = firestore.collection("familiares").get().await().documents
        val familiarToUserMap = familiaresDocs.associate {
            (it.leerEntero("id_familiar") ?: 0) to (it.leerEntero("id_usuario") ?: 0)
        }

        return vinculoDocs.mapNotNull { doc ->
            val idRes = doc.leerEntero("id_residente") ?: return@mapNotNull null
            val idFam = doc.leerEntero("id_familiar") ?: 0
            val idUserFam = doc.leerEntero("id_usuario") ?: familiarToUserMap[idFam] ?: idFam

            val residente = residentes[idRes]
            val usuarioFam = usuariosFamiliares[idUserFam]

            val nomRes = residente?.let { "${it.nombre} ${it.apellido_paterno}".trim() } ?: "Residente #$idRes"
            val habRes = residente?.habitacion ?: "Sin asignar"
            val nomFam = usuarioFam?.let { "${it.nombre} ${it.apellido_paterno.orEmpty()}".trim() } ?: "Familiar #$idFam"
            val correoFam = usuarioFam?.correo ?: ""
            val telFam = usuarioFam?.telefono ?: ""
            val parentesco = doc.getString("parentesco")?.takeIf { it.isNotBlank() } ?: "Familiar"

            ResidenteFamiliarVinculo(
                docId = doc.id,
                idResidente = idRes,
                nombreResidente = nomRes,
                habitacionResidente = habRes,
                idFamiliar = idFam,
                idUsuarioFamiliar = idUserFam,
                nombreFamiliar = nomFam,
                correoFamiliar = correoFam,
                telefonoFamiliar = telFam,
                parentesco = parentesco
            )
        }
    }

    /**
     * Crea una relación entre un residente y un familiar de forma persistente.
     * Previene duplicados.
     */
    suspend fun vincularResidenteFamiliar(
        idResidente: Int,
        idUsuarioFamiliar: Int,
        parentesco: String
    ) {
        require(idResidente > 0) { "Selecciona un residente válido." }
        require(idUsuarioFamiliar > 0) { "Selecciona un usuario familiar válido." }

        // 1. Obtener o registrar id_familiar en la colección 'familiares'
        val idFamiliar = obtenerOcrearIdFamiliar(idUsuarioFamiliar, parentesco)

        // IDs estables para alinear vinculos existentes con las reglas y el cliente PWA.
        val links = firestore.collection("residente_familiar")
        val deterministicId = "${idUsuarioFamiliar}_${idResidente}"
        val deterministicRef = links.document(deterministicId)

        // Recoger el destino determinista y los vinculos legacy del mismo residente/familiar.
        val existentes = links.whereEqualTo("id_residente", idResidente)
            .get().await().documents
        val deterministicSnapshot = deterministicRef.get().await()
        val vinculacionesCoincidentes = (existentes + listOfNotNull(deterministicSnapshot.takeIf { it.exists() }))
            .distinctBy { it.id }
            .filter { doc ->
                doc.id == deterministicId ||
                    doc.leerEntero("id_usuario") == idUsuarioFamiliar ||
                    doc.leerEntero("id_familiar") == idFamiliar
            }

        // Fusionar datos previos; conservar id_familiar y parentesco del registro legacy.
        val camposPrevios = linkedMapOf<String, Any?>()
        vinculacionesCoincidentes.filterNot { it.id == deterministicId }
            .forEach { camposPrevios.putAll(it.data.orEmpty()) }
        deterministicSnapshot.data?.let { camposPrevios.putAll(it) }

        val idFamiliarPersistido = deterministicSnapshot.leerEntero("id_familiar")
            ?: vinculacionesCoincidentes.firstNotNullOfOrNull { it.leerEntero("id_familiar") }
            ?: idFamiliar
        val parentescoPersistido = deterministicSnapshot.getString("parentesco")
            ?.takeIf { it.isNotBlank() }
            ?: vinculacionesCoincidentes.firstNotNullOfOrNull {
                it.getString("parentesco")?.takeIf { value -> value.isNotBlank() }
            }
            ?: parentesco.ifBlank { "Familiar" }

        camposPrevios["id_usuario"] = idUsuarioFamiliar
        camposPrevios["id_familiar"] = idFamiliarPersistido
        camposPrevios["id_residente"] = idResidente
        camposPrevios["parentesco"] = parentescoPersistido

        // Escritura y deduplicacion atomicas; el vinculo determinista sobrevive siempre.
        val batch = firestore.batch()
        batch.set(deterministicRef, camposPrevios)
        vinculacionesCoincidentes.filterNot { it.id == deterministicId }
            .forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    /**
     * Elimina UNICAMENTE el vinculo entre el residente y el familiar,
     * conservando intacto el expediente del residente y la cuenta del usuario.
     */
    suspend fun desvincularResidenteFamiliar(docId: String) {
        require(docId.isNotBlank()) { "ID de vínculo no válido." }
        firestore.collection("residente_familiar").document(docId).delete().await()
    }

    private suspend fun obtenerOcrearIdFamiliar(idUsuario: Int, parentescoDefault: String): Int {
        val familiaresDocs = firestore.collection("familiares")
            .whereEqualTo("id_usuario", idUsuario)
            .limit(1)
            .get()
            .await()
            .documents

        val docExistente = familiaresDocs.firstOrNull()
        if (docExistente != null) {
            docExistente.leerEntero("id_familiar")?.let { return it }
        }

        // Crear registro de familiar si no existe
        val maxId = firestore.collection("familiares")
            .get()
            .await()
            .documents
            .mapNotNull { it.leerEntero("id_familiar") }
            .maxOrNull() ?: 0

        val nuevoIdFamiliar = maxId + 1
        firestore.collection("familiares").document(nuevoIdFamiliar.toString()).set(
            mapOf(
                "id_familiar" to nuevoIdFamiliar,
                "id_usuario" to idUsuario,
                "parentesco" to parentescoDefault.ifBlank { "Familiar" },
                "telefono" to ""
            )
        ).await()

        return nuevoIdFamiliar
    }

    private fun DocumentSnapshot.leerEntero(campo: String): Int? {
        return getLong(campo)?.toInt() ?: getString(campo)?.toIntOrNull()
    }

    private fun DocumentSnapshot.toUsuario(): Usuario {
        val idRol = getLong("id_rol")?.toInt() ?: getString("id_rol")?.toIntOrNull() ?: 3
        return Usuario(
            id_usuario = getLong("id_usuario")?.toInt() ?: getString("id_usuario")?.toIntOrNull() ?: id.toIntOrNull() ?: 0,
            nombre = getString("nombre") ?: "",
            apellido_paterno = getString("apellido_paterno") ?: "",
            apellido_materno = getString("apellido_materno")?.takeIf { it.isNotBlank() },
            correo = getString("correo") ?: "",
            id_rol = idRol,
            rol = getString("rol") ?: "Familiar",
            telefono = getString("telefono")?.takeIf { it.isNotBlank() }
        )
    }

    private fun DocumentSnapshot.toResidente(): Residente {
        val idVal = getLong("id_residente")?.toInt() ?: getString("id_residente")?.toIntOrNull() ?: id.toIntOrNull() ?: 0
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

    companion object {
        private const val TAG = "FamiliaresAdminRepo"
    }
}
