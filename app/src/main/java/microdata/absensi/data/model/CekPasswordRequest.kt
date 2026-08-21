package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class CekPasswordRequest(
    @SerializedName("password_lama")
    val passwordLama: String
)
