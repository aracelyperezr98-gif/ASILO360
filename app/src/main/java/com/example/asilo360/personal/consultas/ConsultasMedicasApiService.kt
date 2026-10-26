package com.example.asilo360.personal.consultas

import com.example.asilo360.data.model.CambiarEstadoConsultaRequest
import com.example.asilo360.data.model.Consulta
import com.example.asilo360.data.model.ConsultaDetailResponse
import com.example.asilo360.data.model.ConsultaOperationResponse
import com.example.asilo360.data.model.ConsultasListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz Retrofit para los endpoints REST PHP de Consultas Médicas del Personal.
 */
interface ConsultasMedicasApiService {

    @GET("personal/consultas/listar.php")
    suspend fun listarConsultas(
        @Query("id_personal") idPersonal: Int? = null,
        @Query("id_usuario") idUsuario: Int? = null,
        @Query("id_residente") idResidente: Int? = null,
        @Query("estado") estado: String? = null,
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null
    ): Response<ConsultasListResponse>

    @GET("personal/consultas/obtener.php")
    suspend fun obtenerConsulta(
        @Query("id") id: Int
    ): Response<ConsultaDetailResponse>

    @POST("personal/consultas/crear.php")
    suspend fun crearConsulta(
        @Body consulta: Consulta
    ): Response<ConsultaOperationResponse>

    @POST("personal/consultas/actualizar.php")
    suspend fun actualizarConsulta(
        @Body consulta: Consulta
    ): Response<ConsultaOperationResponse>

    @POST("personal/consultas/cambiar_estado.php")
    suspend fun cambiarEstado(
        @Body request: CambiarEstadoConsultaRequest
    ): Response<ConsultaOperationResponse>
}
