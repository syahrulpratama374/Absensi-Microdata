package microdata.absensi.data.remote

import microdata.absensi.data.model.GeneralResponse
import microdata.absensi.data.model.CekPasswordRequest
import microdata.absensi.data.model.KonfirmasiUbahPasswordRequest
import microdata.absensi.data.model.AbsenRequest
import microdata.absensi.data.model.AbsenResponse
import microdata.absensi.data.model.LoginRequest
import microdata.absensi.data.model.LoginResponse
import microdata.absensi.data.model.LogoutRequest
import microdata.absensi.data.model.OtpRequest
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

    @POST("verify-otp")
    suspend fun verifyOtp(
        @Header("x-device-id") deviceId: String,
        @Body request: OtpRequest
    ): Response<LoginResponse>

    @POST("absen")
    suspend fun absen(
        @Header("Authorization") token: String,
        @Header("x-device-id") deviceId: String,
        @Body request: AbsenRequest
    ): Response<AbsenResponse>

    @POST("logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<AbsenResponse>

    @POST("cek-password")
    suspend fun cekPassword(
        @Header("Authorization") token: String,
        @Header("x-device-id") deviceId: String,
        @Body request: CekPasswordRequest
    ): Response<GeneralResponse>

    @POST("konfirmasi-ubah-password")
    suspend fun konfirmasiUbahPassword(
        @Header("Authorization") token: String,
        @Header("x-device-id") deviceId: String,
        @Body request: KonfirmasiUbahPasswordRequest
    ): Response<GeneralResponse>
}