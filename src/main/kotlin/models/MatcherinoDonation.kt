package models

data class MatcherinoDonation(val name: String, val amount: Double) : Comparable<MatcherinoDonation> {
    override fun compareTo(other: MatcherinoDonation): Int {
        return if (amount != other.amount) {
            other.amount.compareTo(amount) // price desc
        } else {
            name.compareTo(other.name, true) // name asc
        }
    }
}