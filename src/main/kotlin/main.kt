import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.UnstableDefault
import models.Donation
import models.Donator
import models.ItemDetails
import models.ParsedLine
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

const val ITEMS_PER_ROW = 4

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
        ParsedLine(columns[0].toLowerCase(), columns[1].toInt(), columns[3].toInt())
    }

    val itemsIds = parsedLines.map { it.itemId }.toSet()
    GlobalScope.launch {
        val items = Requests.fetchItemDetails(itemsIds)
        println(items)

        val prices = Requests.fetchItemPrices(itemsIds)
        println(prices)

        val itemsWithPrice = items.mapNotNull { item ->
            val price = prices.find { it.id == item.id } ?: return@mapNotNull null
            item.copy(price = price.sellsOrBuys.unitPrice)
        }.associateBy { it.id }.toMutableMap()
        itemsWithPrice[-1] = ItemDetails(-1, "gold", "https://i.imgur.com/BYDgrSM.png", 10000)
        itemsWithPrice[-2] = ItemDetails(
            -2,
            "Black Lion Chest Key",
            "https://render.guildwars2.com/file/207BDD31BC494A07A0A1691705079100066D3F2F/414998.png",
            28 * 10000
        )
        println(itemsWithPrice)

        val donatorNames = parsedLines.map { it.name }.toSet()
        val donators = donatorNames.map { name ->
            val filteredName = parsedLines.filter { it.name == name }
            val itemQuantities =
                filteredName.groupingBy { it.itemId }.fold(0) { accumulator, element -> accumulator + element.quantity }
            val donations = itemQuantities.map { itemQuantity ->
                Donation(itemsWithPrice.getValue(itemQuantity.key), itemQuantity.value)
            }.sortedDescending()
            Donator(name, donations)
        }.sortedDescending()

//        println(donators)
        donators.forEach { println("${it.name}: ${it.totalDonation}") }
        printDonations(donators)

        val totalItemQuantities =
            parsedLines.groupingBy { it.itemId }.fold(0) { accumulator, element -> accumulator + element.quantity }
        val totalDonations = totalItemQuantities.map { itemQuantity ->
            Donation(itemsWithPrice.getValue(itemQuantity.key), itemQuantity.value)
        }.sortedDescending()
        println(totalDonations)
        printTotalDonation(totalDonations)
    }
}

fun printTotalDonation(totalDonations: List<Donation>) {
    val output = document.getElementById("output") as HTMLDivElement
    output.append.div {
        span("donator") { +"Total donations" }
        table {
            classes = setOf("totalDonations")
            tr {
                th {
                    colSpan = "2"
                    +"Item"
                }
                th { +"Price" }
            }
            totalDonations.forEach {
                renderTotalDonation(it)
            }
        }
    }
}

fun TABLE.renderTotalDonation(donation: Donation) {
    tr {
        td {
            img {
                alt = donation.item.name
                src = donation.item.icon
                width = "32px"
                height = "32px"
                style = "vertical-align: middle;"
            }
        }
        td {
            style = "text-align: left;"
            if (donation.quantity >= 2) {
                +"${donation.quantity} "
            }
            +donation.item.name
        }
        td {
            style = "text-align: right;"
            img {
                alt = "Gold"
                src = "https://i.imgur.com/BYDgrSM.png"
                width = "16px"
                height = "16px"
                classes = setOf("totalValueGoldIcon")
            }
            +(donation.totalPrice / 10000).toString()
        }
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
    val itemRows = donator.donations.chunked(ITEMS_PER_ROW)

    table("donationsTable") {
        itemRows.forEach { row ->
            if (row.size % 2 == ITEMS_PER_ROW % 2) {
                renderItems(row)
            }
        }
    }

    val lastRow = itemRows.last()
    if (lastRow.size % 2 != ITEMS_PER_ROW % 2) {
        table("donationsTable") { renderItems(lastRow) }
    }

    val totalValue = donator.donations.fold(0) { accumulator, donation -> accumulator + donation.totalPrice }
    div("itemContainer totalValue") {
        +"Total value: "
        img {
            alt = "Gold"
            src = "https://i.imgur.com/BYDgrSM.png"
            width = "32px"
            height = "32px"
            classes = setOf("totalValueGoldIcon")
        }
        +(totalValue / 10000).toString()
    }
}

private fun TABLE.renderItems(items: List<Donation>) = tr {
    val fakeTdCount = (ITEMS_PER_ROW - items.size) / 2
    for (i in 0 until fakeTdCount) {
        td {}
    }
    items.forEach { renderItem(it) }
    for (i in 0 until fakeTdCount) {
        td {}
    }
}

private fun TR.renderItem(donation: Donation) = td {
    div("itemContainer") {
        //    div(classes = "itemContainer" + if (center) " centerContent" else "") {
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
}