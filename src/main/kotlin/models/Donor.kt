package models

import ONE_GOLD

data class Donor(val name: String, val donations: List<Donation>, val leftImage: String?, val rightImage: String?) :
    Comparable<Donor> {
    val totalDonation: Long
        get() = donations.fold(0L) { acc, donation -> acc + donation.totalPrice }

    val isBigDonor: Boolean by lazy { totalDonation >= 1000 * ONE_GOLD }

    override fun compareTo(other: Donor): Int {
        return totalDonation.compareTo(other.totalDonation)
    }
}