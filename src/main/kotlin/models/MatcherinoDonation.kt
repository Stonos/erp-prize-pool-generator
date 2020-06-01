package models

data class MatcherinoDonation(val matcherinoDonor: MatcherinoDonor, val amount: Double) :
    Comparable<MatcherinoDonation> {
    override fun compareTo(other: MatcherinoDonation): Int {
        return if (amount != other.amount) {
            other.amount.compareTo(amount) // price desc
        } else {
            matcherinoDonor.name.compareTo(other.matcherinoDonor.name, true) // name asc
        }
    }
}