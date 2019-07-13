import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.js.div
import kotlinx.html.span
import kotlinx.serialization.UnstableDefault
import models.Donation
import models.Donator
import models.ItemDetails
import models.ParsedLine
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

fun main() {
    val txtInput = document.getElementById("txtInput") as HTMLTextAreaElement
    val btnGo = document.getElementById("btnGo") as HTMLButtonElement
    btnGo.addEventListener("click", {
        parseInput(txtInput.value)
    })
}

@UnstableDefault
private fun parseInput(input: String) {
    val lines = input.split("\n")
    val parsedLines = lines.map { line ->
        val columns = line.split("\t")
        ParsedLine(columns[0], columns[1].toInt(), columns[3].toInt())
    }

    val itemsIds = parsedLines.map { it.itemId }.toSet()
    GlobalScope.launch {
        val items = Requests.fetchItemDetails(itemsIds)
        println(items)

        val prices = Requests.fetchItemPrices(itemsIds)
        println(prices)

        val itemsWithPrice = items.mapNotNull { item ->
            val price = prices.find { it.id == item.id } ?: return@mapNotNull null
            item.copy(price = price.sells.unitPrice)
        }.associateBy { it.id }.toMutableMap()
        itemsWithPrice[-1] = ItemDetails(-1, "gold", "https://wiki.guildwars2.com/images/d/d1/Gold_coin.png", 10000)
        println(itemsWithPrice)

        val donatorNames = parsedLines.map { it.name }.toSet()
        val donators = donatorNames.map { name ->
            val donations = parsedLines.filter { it.name == name }.map { parsedLine ->
                Donation(itemsWithPrice.getValue(parsedLine.itemId), parsedLine.quantity)
            }.sortedDescending()
            Donator(name, donations)
        }.sortedDescending()

//        println(donators)
        donators.forEach { println("${it.name}: ${it.totalDonation}") }
        printDonations(donators)
    }
}

fun printDonations(donators: List<Donator>) {
    val output = document.getElementById("output") as HTMLDivElement
    output.append.div {
        donators.forEach {
            renderDonator(it)
        }
    }
}

private fun DIV.renderDonator(donator: Donator) = div("donator") {
    span("donator") { +donator.name }
    val itemRows = donator.donations.chunked(4)
    itemRows.forEach { renderItems(it) }
}

private fun DIV.renderItems(items: List<Donation>) = div("items") {
    items.forEach { renderItem(it) }
}

private fun DIV.renderItem(donation: Donation) = div(classes = "itemContainer") {
    img {
        alt = donation.item.name
        src = donation.item.icon
        width = "64px"
        height = "64px"
    }
    span("multiplicationSign") {
        +"×"
    }
    +donation.quantity.toString()
}