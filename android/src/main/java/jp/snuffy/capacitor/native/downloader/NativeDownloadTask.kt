package jp.snuffy.capacitor

import com.google.gson.annotations.SerializedName
import com.tonyodev.fetch2.Request

data class NativeDownloadTask(
        @SerializedName("id") val id: String,
        @SerializedName("url") val url: String,
        @SerializedName("size") val size: Int,
        @SerializedName("filePath") val filePath: String,
        @SerializedName("fileName") val fileName: String,
        @SerializedName("displayName") val displayName: String,
        @SerializedName("headers") val headers: MutableMap<String, String>,
        @SerializedName("request") var request: Request? = null,
        @SerializedName("index") var index: Long
)
