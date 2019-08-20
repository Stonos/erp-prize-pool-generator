package models

data class Donator(val name: String, val donations: List<Donation>) : Comparable<Donator> {
    val totalDonation: Long
        get() = donations.fold(0L) { acc, donation -> acc + donation.totalPrice }

    override fun compareTo(other: Donator): Int {
        return totalDonation.compareTo(other.totalDonation)
    }
}