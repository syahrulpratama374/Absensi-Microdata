package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class AbsenResponse(
    @SerializedName("message")
    val message: String?
)
