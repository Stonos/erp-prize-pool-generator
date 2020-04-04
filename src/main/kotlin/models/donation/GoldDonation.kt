package models.donation

import COPPER_ICON
import GOLD_ICON
import ONE_GOLD
import ONE_SILVER
import SILVER_ICON
import kotlinx.html.HtmlBlockTag
import kotlinx.html.classes
import kotlinx.html.img

class GoldDonation(override val totalPrice: Long) : Donation() {
    private val isOverOneGold = (totalPrice / ONE_GOLD) >= 1
    private val isOverOneSilver = (totalPrice / ONE_SILVER) >= 1

    override val name = when {
        isOverOneGold -> "Gold"
        isOverOneSilver -> "Silver"
        else -> "Copper"
    }

    override val icon = when {
        isOverOneGold -> GOLD_ICON
        isOverOneSilver -> SILVER_ICON
        else -> COPPER_ICON
    }

    override val quantity = when {
        isOverOneGold -> totalPrice / ONE_GOLD
        isOverOneSilver -> totalPrice / ONE_SILVER
        else -> totalPrice
    }

    fun printTotalAmount(tag: HtmlBlockTag) = with(tag) {
        +quantity.toString()
        img {
            alt = name
            src = icon
            width = "32px"
            height = "32px"
            classes = setOf("totalValueGoldIcon")
        }
    }
}