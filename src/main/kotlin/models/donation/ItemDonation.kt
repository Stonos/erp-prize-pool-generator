package models.donation

import models.ItemDetails

data class ItemDonation(val item: ItemDetails, override val quantity: Long) : Donation() {
    override val name = item.name
    override val icon = item.icon

    override val totalPrice: Long
        get() = item.price * quantity
}