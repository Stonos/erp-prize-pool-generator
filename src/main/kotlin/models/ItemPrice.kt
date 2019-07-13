package models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemPrice(
    @SerialName("id")
    val id: Int,

    @SerialName("buys")
    val buys: ItemQuantityPrice,

    @SerialName("sells")
    val sells: ItemQuantityPrice
)