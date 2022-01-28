package com.harry.scprprograms.repository

import com.harry.scprprograms.api.RetrofitInstance

class SCPRProgramsRepository {
    suspend fun getPrograms() = RetrofitInstance.api.fetchPrograms()

    suspend fun getProgramClips(programId: String) =
        RetrofitInstance.api.fetchProgramClips(programId)
}