package com.example.menuannam

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface NetworkService {
    @POST
    suspend fun generateToken(
        @Url url: String = "https://egsbwqh7kildllpkijk6nt4soq0wlgpe.lambda-url.ap-southeast-1.on.aws/",
        @Body email: UserCredential
    ): TokenResponse
}