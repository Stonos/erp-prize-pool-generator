package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetails(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String,

    @SerialName("icon")
    val icon: String,

    val price: Long = 0L
)