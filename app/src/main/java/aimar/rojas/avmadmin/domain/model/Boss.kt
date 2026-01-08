package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Boss(
    @SerializedName("boss_id")
    val bossId: Int,
    @SerializedName("name_boss")
    val nameBoss: String,
    @SerializedName("boss_type")
    val bossType: String,
    @SerializedName("user_id")
    val userId: Int
) : Parcelable