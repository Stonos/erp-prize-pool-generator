package models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemQuantityPrice(
    @SerialName("quantity")
    val quantity: Int,

    @SerialName("unit_price")
    val unitPrice: Int
)