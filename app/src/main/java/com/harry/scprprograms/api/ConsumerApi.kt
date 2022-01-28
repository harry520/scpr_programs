package com.harry.scprprograms.api

import com.harry.scprprograms.model.Clips
import com.harry.scprprograms.model.Programs
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ConsumerApi {
    @GET("programs")
    suspend fun fetchPrograms(): Response<Programs>

    @GET("programs/{programId}/clips")
    suspend fun fetchProgramClips(
        @Path("programId")
        programId: String
    ): Response<Clips>
}