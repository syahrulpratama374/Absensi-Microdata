package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class KonfirmasiUbahPasswordRequest(
    @SerializedName("otp")
    val otp: String,

    @SerializedName("password_baru")
    val passwordBaru: String,

    @SerializedName("konfirmasi_password_baru")
    val konfirmasiPasswordBaru: String
)
