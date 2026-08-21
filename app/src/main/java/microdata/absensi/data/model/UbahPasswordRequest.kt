package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class UbahPasswordRequest(
    @SerializedName("password_lama")
    val passwordLama: String,

    @SerializedName("password_baru")
    val passwordBaru: String
)