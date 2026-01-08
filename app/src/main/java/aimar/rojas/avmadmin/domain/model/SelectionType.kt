package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectionType(
    @SerializedName("selection_type_id")
    val selectionTypeId: Int,
    @SerializedName("name_selection")
    val nameSelection: String
) : Parcelable