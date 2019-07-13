package models

data class Donator(val name: String, val donations: List<Donation>) : Comparable<Donator> {
    val totalDonation: Int
        get() = donations.fold(0) { acc, donation -> acc + donation.totalPrice }

    override fun compareTo(other: Donator): Int {
        return totalDonation.compareTo(other.totalDonation)
    }
}