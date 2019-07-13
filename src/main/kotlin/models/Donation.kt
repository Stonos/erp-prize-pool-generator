package models

data class Donation(val item: ItemDetails, val quantity: Int) : Comparable<Donation> {
    val totalPrice: Int
        get() = item.price * quantity

    override fun compareTo(other: Donation): Int {
        return totalPrice.compareTo(other.totalPrice)
    }
}