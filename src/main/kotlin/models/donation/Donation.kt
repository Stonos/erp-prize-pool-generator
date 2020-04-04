package models.donation

import GOLD_ID
import models.ItemDetails

abstract class Donation : Comparable<Donation> {
    abstract val name: String
    abstract val icon: String
    abstract val quantity: Long
    abstract val totalPrice: Long

    override fun compareTo(other: Donation): Int {
        return totalPrice.compareTo(other.totalPrice)
    }

    companion object {
        fun buildDonation(itemsMap: Map<Int, ItemDetails>, itemId: Int, quantity: Int): Donation {
            return if (itemId != GOLD_ID) {
                val item = itemsMap[itemId] ?: error("Item with ID $itemId not found")
                ItemDonation(item, quantity.toLong())
            } else {
                GoldDonation(quantity.toLong())
            }
        }
    }
}