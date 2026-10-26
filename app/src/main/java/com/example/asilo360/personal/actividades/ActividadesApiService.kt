package com.example.asilo360.personal.actividades

import com.example.asilo360.data.model.Actividad
import com.example.asilo360.data.model.ActividadDetailResponse
import com.example.asilo360.data.model.ActividadOperationResponse
import com.example.asilo360.data.model.ActividadesListResponse
import com.example.asilo360.data.model.EliminarActividadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz Retrofit para los endpoints REST PHP de Actividades del Personal.
 */
interface ActividadesApiService {

    @GET("personal/actividades/listar.php")
    suspend fun listarActividades(
        @Query("id_personal") idPersonal: Int? = null,
        @Query("id_usuario") idUsuario: Int? = null,
        @Query("id_residente") idResidente: Int? = null,
        @Query("tipo") tipo: String? = null,
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null
    ): Response<ActividadesListResponse>

    @GET("personal/actividades/obtener.php")
    suspend fun obtenerActividad(
        @Query("id") id: Int
    ): Response<ActividadDetailResponse>

    @POST("personal/actividades/crear.php")
    suspend fun crearActividad(
        @Body actividad: Actividad
    ): Response<ActividadOperationResponse>

    @POST("personal/actividades/actualizar.php")
    suspend fun actualizarActividad(
        @Body actividad: Actividad
    ): Response<ActividadOperationResponse>

    @POST("personal/actividades/eliminar.php")
    suspend fun eliminarActividad(
        @Body request: EliminarActividadRequest
    ): Response<ActividadOperationResponse>
}
