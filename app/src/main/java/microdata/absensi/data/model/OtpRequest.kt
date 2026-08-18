package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("otp")
    val otp: String,

    @SerializedName("otp_token")
    val otpToken: String,

    @SerializedName("device_id")
    val deviceId: String
)