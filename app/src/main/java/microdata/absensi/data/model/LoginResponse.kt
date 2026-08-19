package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("otp_token")
    val otpToken: String?,

    @SerializedName("access_token")
    val accessToken: String?,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("message")
    val message: String?
)