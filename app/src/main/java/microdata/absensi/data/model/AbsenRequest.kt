package microdata.absensi.data.model

import com.google.gson.annotations.SerializedName

data class AbsenRequest(
    @SerializedName("kode_jenis")
    val kodeJenis: String,

    @SerializedName("koordinat")
    val koordinat: String,

    @SerializedName("keterangan")
    val keterangan: String,

    @SerializedName("file_bukti")
    val fileBukti: String? = null
)
