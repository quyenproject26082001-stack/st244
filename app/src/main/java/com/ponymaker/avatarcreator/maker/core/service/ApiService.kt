package com.ponymaker.avatarcreator.maker.core.service
import com.ponymaker.avatarcreator.maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST239_PonyMakerOCMaker5")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}