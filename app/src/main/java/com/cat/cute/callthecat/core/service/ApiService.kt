package com.cat.cute.callthecat.core.service
import com.cat.cute.callthecat.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST239_PonyMakerOCMaker5")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}