package com.example.asilo360.personal.residentes

import com.example.asilo360.data.model.ResidenteDetailResponse
import com.example.asilo360.data.model.ResidentesListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio Retrofit para consultar endpoints REST PHP de Residentes (Rol Personal).
 */
interface ResidentesPersonalApiService {

    @GET("personal/residentes/listar.php")
    suspend fun listarResidentes(
        @Query("id_rol") idRol: Int = 2
    ): Response<ResidentesListResponse>

    @GET("personal/residentes/obtener.php")
    suspend fun obtenerResidente(
        @Query("id") id: Int,
        @Query("id_rol") idRol: Int = 2
    ): Response<ResidenteDetailResponse>
}
