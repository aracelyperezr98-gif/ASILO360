package com.example.asilo360.data.firebase

import com.example.asilo360.data.model.RegistroFirebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RegistroRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val registros = firestore.collection("registros")

    suspend fun guardar(registro: RegistroFirebase): String {
        val documento = if (registro.id.isBlank()) registros.document() else registros.document(registro.id)
        documento.set(registro.copy(id = documento.id)).await()
        return documento.id
    }

    suspend fun listar(): List<RegistroFirebase> = registros.get().await().documents.mapNotNull { documento ->
        documento.toObject(RegistroFirebase::class.java)?.copy(id = documento.id)
    }

    suspend fun eliminar(id: String) {
        registros.document(id).delete().await()
    }
}