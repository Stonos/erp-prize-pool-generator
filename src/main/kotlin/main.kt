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
const val ITEMS_PER_ROW_SMALL = 4
const val SMALL_DONATIONS_PER_ROW = 2
const val GOLD_ICON = "https://i.imgur.com/mV00LQE.png"
const val ONE_GOLD = 10000L

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
        val chunkedIds = itemsIds.chunked(200)

        val items = chunkedIds.map { Requests.fetchItemDetails(it) }.flatten()
        println(items)

        val prices = chunkedIds.map { Requests.fetchItemPrices(it) }.flatten()
        println(prices)

        val itemsWithPrice = items.mapNotNull { item ->
            val price = prices.find { it.id == item.id } ?: return@mapNotNull null
            item.copy(price = price.sellsOrBuys.unitPrice)
        }.associateBy { it.id }.toMutableMap()
        itemsWithPrice[-1] = ItemDetails(-1, "gold", GOLD_ICON, ONE_GOLD)
        itemsWithPrice[-2] = ItemDetails(
            -2,
            "Black Lion Chest Key",
            "https://render.guildwars2.com/file/207BDD31BC494A07A0A1691705079100066D3F2F/414998.png",
            28 * ONE_GOLD
        )
        itemsWithPrice[-3] = ItemDetails(
            -3,
            "Orb of Crystallized Plasma",
            "https://render.guildwars2.com/file/034B091471E6067C2B0BCC70FE04D2F3AE51F291/1010539.png",
            100 * ONE_GOLD
        )
        itemsWithPrice[-4] = ItemDetails(
            -4,
            "Chunk of Crystallized Plasma",
            "https://render.guildwars2.com/file/B55C52B1117B0AE9C124FF40DD5E4D0A5295095F/1010533.png",
            1 * ONE_GOLD
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
    val totalValue = totalDonations.fold(0L) { accumulator, element -> accumulator + element.totalPrice }

    val output = document.getElementById("output") as HTMLDivElement
    output.append.div {
        span("donator") { +"Total donations" }
        table {
            classes = setOf("totalDonations")
//            tr {
//                th {
//                    colSpan = "2"
//                    +"Item"
//                }
//                th { +"Price" }
//            }
            totalDonations.forEach {
                renderTotalDonation(it)
            }
            tr {
                td { }
                td {
                    style = "text-align: left;"
                    +"Total"
                }
                printPriceCell(totalValue)
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
                classes = setOf("itemIcon")
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
        printPriceCell(donation.totalPrice)
    }
}

private fun TR.printPriceCell(totalPrice: Long) {
    td {
        style = "text-align: right; vertical-align: middle;"
        +(totalPrice / ONE_GOLD).toString()
        img {
            alt = "Gold"
            src = GOLD_ICON
            width = "32px"
            height = "32px"
            classes = setOf("totalValueGoldIcon")
        }
    }
}

fun printDonations(donators: List<Donator>) {
    val output = document.getElementById("output") as HTMLDivElement
    val bigDonators = donators.filter { it.isBigDonator }
    val smallDonators = donators.filter { !it.isBigDonator }.chunked(SMALL_DONATIONS_PER_ROW)
    output.append.div {
        bigDonators.forEach {
            renderDonator(it)
        }

        table {
            classes = setOf("smallDonationsTable")
            smallDonators.forEach { row ->
                tr {
                    row.forEach {
                        td {
                            div {
                                renderDonator(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DIV.renderDonator(donator: Donator) = div("donator" + if (!donator.isBigDonator) " smallDonator" else "") {
    val itemsPerRow = if (donator.isBigDonator) ITEMS_PER_ROW else ITEMS_PER_ROW_SMALL
    span("donator") { +donator.name }
    val itemRows = donator.donations.chunked(itemsPerRow)

    table("donationsTable") {
        itemRows.forEach { row ->
            if (row.size % 2 == itemsPerRow % 2) {
                renderItems(row, donator.isBigDonator, itemsPerRow)
            }
        }
    }

    val lastRow = itemRows.last()
    if (lastRow.size % 2 != itemsPerRow % 2) {
        table("donationsTable") { renderItems(lastRow, donator.isBigDonator, itemsPerRow) }
    }

    val totalValue = donator.donations.fold(0L) { accumulator, donation -> accumulator + donation.totalPrice }
    div("itemContainer totalValue") {
        +"Total value: "
        +(totalValue / ONE_GOLD).toString()
        img {
            alt = "Gold"
            src = GOLD_ICON
            width = "32px"
            height = "32px"
            classes = setOf("totalValueGoldIcon")
        }
    }
}

private fun TABLE.renderItems(items: List<Donation>, isBigDonator: Boolean, itemsPerRow: Int) = tr {
    val fakeTdCount = (itemsPerRow - items.size) / 2
    for (i in 0 until fakeTdCount) {
        td {}
    }
    items.forEach { renderItem(it, isBigDonator) }
    for (i in 0 until fakeTdCount) {
        td {}
    }
}

private fun TR.renderItem(donation: Donation, bigDonator: Boolean) = td {
    val imageSize = if (bigDonator) "64px" else "32px"
    div("itemContainer") {
        //    div(classes = "itemContainer" + if (center) " centerContent" else "") {
        img {
            alt = donation.item.name
            src = donation.item.icon
            classes = setOf("itemIcon")
            width = imageSize
            height = imageSize
        }
        span("multiplicationSign") {
            +"×"
        }
        +donation.quantity.toString()
    }
}