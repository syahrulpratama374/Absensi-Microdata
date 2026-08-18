package microdata.absensi.data.remote

import microdata.absensi.data.model.LoginRequest
import microdata.absensi.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(
        @Header("x-device-id") deviceId: String,
        @Body request: LoginRequest
    ): Response<LoginResponse>
}