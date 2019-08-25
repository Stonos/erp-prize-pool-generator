package models

import ONE_GOLD

data class Donator(val name: String, val donations: List<Donation>, val leftImage: String?, val rightImage: String?) :
    Comparable<Donator> {
    val totalDonation: Long
        get() = donations.fold(0L) { acc, donation -> acc + donation.totalPrice }

    val isBigDonator: Boolean by lazy { totalDonation >= 1000 * ONE_GOLD }

    override fun compareTo(other: Donator): Int {
        return totalDonation.compareTo(other.totalDonation)
    }
}