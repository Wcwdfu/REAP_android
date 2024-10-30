package com.reap.data.remote.api

import com.reap.domain.model.RecognizeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MainApi {
    @Multipart
    @POST("/auth/recognize-url")
    suspend fun postRecognizeUrl(
        @Part("topic") topic: RequestBody,
        @Part media: MultipartBody.Part
    ): RecognizeResponse
}
