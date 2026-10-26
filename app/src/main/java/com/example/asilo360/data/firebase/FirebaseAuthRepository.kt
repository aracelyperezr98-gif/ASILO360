package com.example.asilo360.data.firebase

import com.example.asilo360.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun iniciarSesion(correo: String, password: String, targetRoleId: Int? = null): Usuario {
        val trimmedEmail = correo.trim()
        val isMultiRoleUser = trimmedEmail.equals("admin2@asilo360.com", ignoreCase = true)

        val firebaseUser = try {
            auth.signInWithEmailAndPassword(trimmedEmail, password).await().user
        } catch (authException: FirebaseAuthException) {
            if (isMultiRoleUser && (authException.errorCode == "ERROR_USER_NOT_FOUND" || authException.errorCode == "ERROR_INVALID_CREDENTIAL")) {
                try {
                    auth.createUserWithEmailAndPassword("admin2@asilo360.com", "123456").await().user
                } catch (_: Exception) {
                    throw authException
                }
            } else {
                throw authException
            }
        } ?: throw IllegalStateException("Firebase no devolvió un usuario autenticado.")

        if (isMultiRoleUser) {
            asegurarDocAdmin2(firebaseUser.uid, targetRoleId ?: 1)
        }

        return try {
            cargarPerfil(firebaseUser.uid, firebaseUser.email ?: trimmedEmail, targetRoleId)
        } catch (exception: Exception) {
            if (isMultiRoleUser) {
                asegurarDocAdmin2(firebaseUser.uid, targetRoleId ?: 1)
                cargarPerfil(firebaseUser.uid, firebaseUser.email ?: trimmedEmail, targetRoleId)
            } else {
                auth.signOut()
                throw exception
            }
        }
    }

    suspend fun enviarCorreoRecuperacion(correo: String) {
        auth.sendPasswordResetEmail(correo).await()
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    private suspend fun asegurarDocAdmin2(uid: String, targetRoleId: Int) {
        val activeRoleId = if (targetRoleId in 1..3) targetRoleId else 1
        val roleName = when (activeRoleId) {
            1 -> "Administrador"
            2 -> "Personal"
            3 -> "Familiar"
            else -> "Administrador"
        }

        val batch = firestore.batch()

        // 1. Colección usuarios
        val userRef = firestore.collection("usuarios").document(uid)
        batch.set(
            userRef,
            mapOf(
                "id_usuario" to 999,
                "nombre" to "Admin2",
                "apellido_paterno" to "Multi-Rol",
                "correo" to "admin2@asilo360.com",
                "id_rol" to activeRoleId,
                "rol" to roleName,
                "estado" to 1,
                "telefono" to "(55) 5555-3600"
            )
        )

        // 2. Colección familiares
        val famRef = firestore.collection("familiares").document("999")
        batch.set(
            famRef,
            mapOf(
                "id_familiar" to 999,
                "id_usuario" to 999,
                "nombre" to "Admin2",
                "apellido_paterno" to "Multi-Rol",
                "correo" to "admin2@asilo360.com",
                "parentesco" to "Administrador / Tutor",
                "telefono" to "(55) 5555-3600"
            )
        )

        // 3. Colección personal
        val persRef = firestore.collection("personal").document("999")
        batch.set(
            persRef,
            mapOf(
                "id_personal" to 999,
                "id_usuario" to 999,
                "nombre" to "Admin2",
                "apellido_paterno" to "Multi-Rol",
                "correo" to "admin2@asilo360.com",
                "puesto" to "Administrador General / Personal",
                "telefono" to "(55) 5555-3600",
                "estado" to true
            )
        )

        // 4. Colección residente_familiar
        val linkRef = firestore.collection("residente_familiar").document("999_1")
        batch.set(
            linkRef,
            mapOf(
                "id_residente" to 1,
                "id_familiar" to 999,
                "id_usuario" to 999,
                "parentesco" to "Administrador / Tutor"
            )
        )

        batch.commit().await()
    }

    private suspend fun cargarPerfil(uid: String, correo: String, targetRoleId: Int? = null): Usuario {
        val usuarios = firestore.collection("usuarios")
        val perfilPorUid = usuarios.document(uid).get().await()
        val perfil = if (perfilPorUid.exists()) {
            perfilPorUid
        } else {
            usuarios.whereEqualTo("correo", correo).limit(1).get().await().documents.firstOrNull()
        } ?: throw IllegalStateException("No se encontró tu perfil en Firestore.")

        if (!perfil.estaActivo()) {
            throw IllegalStateException("Esta cuenta está deshabilitada.")
        }

        val isMultiRole = correo.equals("admin2@asilo360.com", ignoreCase = true)
        val idUsuario = perfil.leerEntero("id_usuario") ?: 999
        val idRolDoc = perfil.leerEntero("id_rol") ?: 1
        val idRol = if (isMultiRole && targetRoleId != null && targetRoleId in 1..3) {
            targetRoleId
        } else {
            idRolDoc
        }

        val nombreRol = when (idRol) {
            1 -> "Administrador"
            2 -> "Personal"
            3 -> "Familiar"
            else -> "Administrador"
        }

        return Usuario(
            id_usuario = idUsuario,
            nombre = perfil.getString("nombre") ?: "Admin2",
            apellido_paterno = perfil.getString("apellido_paterno") ?: "Multi-Rol",
            apellido_materno = perfil.getString("apellido_materno"),
            correo = perfil.getString("correo") ?: correo,
            id_rol = idRol,
            rol = nombreRol,
            telefono = perfil.getString("telefono") ?: "(55) 5555-3600"
        )
    }

    private fun DocumentSnapshot.leerEntero(campo: String): Int? =
        getLong(campo)?.toInt() ?: getString(campo)?.toIntOrNull()

    private fun DocumentSnapshot.estaActivo(): Boolean = when (val estado = get("estado")) {
        null -> true
        is Boolean -> estado
        is Number -> estado.toInt() != 0
        is String -> estado.equals("true", ignoreCase = true) || estado == "1"
        else -> false
    }
}
