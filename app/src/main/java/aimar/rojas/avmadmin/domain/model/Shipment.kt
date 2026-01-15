package aimar.rojas.avmadmin.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Shipment(
    @SerializedName("shipment_id")
    val shipmentId: Int,
    @SerializedName("start_date")
    val startDate: Date,
    @SerializedName("end_date")
    val endDate: Date? = null,
    val status: String,
    @SerializedName("amount_per_shipment")
    val amountPerShipment: Double
) : Parcelable