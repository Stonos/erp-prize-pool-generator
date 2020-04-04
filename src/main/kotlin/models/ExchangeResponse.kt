package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeResponse(
    @SerialName("coins_per_gem")
    val coinsPerGem: Long,

    @SerialName("quantity")
    val quantity: Int
)