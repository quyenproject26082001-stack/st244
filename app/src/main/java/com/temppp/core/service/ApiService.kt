package com.temppp.core.service
import com.temppp.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST239_PonyMakerOCMaker5")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}