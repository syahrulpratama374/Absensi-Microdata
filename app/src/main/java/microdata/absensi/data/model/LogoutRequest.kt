package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("device_id")
    val deviceId: String
)