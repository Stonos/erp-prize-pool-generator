import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import models.*
import models.donation.Donation
import models.donation.GoldDonation
import org.w3c.dom.HTMLDivElement
import kotlin.math.round

const val ITEMS_PER_ROW = 4
const val ITEMS_PER_ROW_SMALL = 4

fun main() {}

@JsExport
@JsName("parseDonations")
fun parseDonations(
    input: String,
    matcherinoInput: String,
    emotesInput: String,
    helpersInput: String,
    customIdsInput: String,
    outputDiv: HTMLDivElement
) {
    val emotes = parseEmotes(emotesInput)

    parseHelpers(helpersInput, emotes, outputDiv)

    parseMatcherino(matcherinoInput, outputDiv)

    val lines = input.split("\n")
    val parsedLines = lines.map { line ->
        val columns = line.split("\t")

        val itemId = columns[3].toInt()
        val quantity = if (itemId != GOLD_ID) {
            columns[1].toInt()
        } else {
            (columns[1].toFloat() * ONE_GOLD).toInt()
        }
        ParsedLine(
            name = columns[0].toLowerCase(),
            quantity = quantity,
            itemId = itemId
        )
    }

    println(emotes)

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
        addCustomIds(itemsWithPrice, customIdsInput)
        println(itemsWithPrice)

        val donorNames = parsedLines.map { it.name }.toSet()
        val donors = donorNames.map { name ->
            val filteredName = parsedLines.filter { it.name == name }
            val itemQuantities =
                filteredName.groupingBy { it.itemId }.fold(0) { accumulator, element -> accumulator + element.quantity }
            val donations = itemQuantities.map { itemQuantity ->
                Donation.buildDonation(itemsWithPrice, itemQuantity.key, itemQuantity.value)
            }.sortedDescending()

            val emote = emotes[name]
            Donor(name, donations, emote?.first, emote?.second)
        }.sortedDescending()

        donors.forEach { println("${it.name}: ${it.totalDonation}") }
        printDonations(donors, outputDiv)

        val totalItemQuantities =
            parsedLines.groupingBy { it.itemId }.fold(0) { accumulator, element -> accumulator + element.quantity }
        val totalDonations = totalItemQuantities.map { itemQuantity ->
            Donation.buildDonation(itemsWithPrice, itemQuantity.key, itemQuantity.value)
        }.sortedDescending()

        println(totalDonations)
        printTotalDonation(totalDonations, outputDiv)
    }
}

fun parseHelpers(helpersInput: String, emotes: Map<String, Pair<String?, String?>>, outputDiv: HTMLDivElement) {
    val helpers = helpersInput.split("\n")
    printHelpers(helpers, emotes, outputDiv)
}

fun printHelpers(helpers: List<String>, emotes: Map<String, Pair<String?, String?>>, outputDiv: HTMLDivElement) {
    outputDiv.append.div {
        span("donor title") {
            +"Helpers"
        }
        table {
            classes = setOf("totalDonations")
            helpers.forEach { helper ->
                val emotePair = emotes[helper.toLowerCase()]
                tr {
                    td(classes = "donor title") {
                        if (!emotePair?.first.isNullOrBlank()) {
                            img("", emotePair?.first, "titleImage smallImage")
                        }

                        +helper

                        if (!emotePair?.second.isNullOrBlank()) {
                            img("", emotePair?.second, "titleImage")
                        }
                    }
                }
            }
        }
    }
}

private fun parseEmotes(emotesInput: String): Map<String, Pair<String?, String?>> {
    val emoteLines = emotesInput.split("\n")
    val emotes = emoteLines.associate { line ->
        val columns = line.split("\t")
        Pair(columns[0].toLowerCase(), Pair(columns.getOrNull(1), columns.getOrNull(4)))
    }
    return emotes
}

private fun addCustomIds(itemsWithPrice: MutableMap<Int, ItemDetails>, customIdsInput: String) {
    val lines = customIdsInput.split("\n")
    lines.forEach { line ->
        val columns = line.split("\t")
        val id = columns[0].toInt()
        val name = columns[1]
        val icon = columns[2]
        val price = columns[3].toLong()
        itemsWithPrice[id] = ItemDetails(id, name, icon, price)
    }
}

fun parseMatcherino(input: String, outputDiv: HTMLDivElement) {
    val lines = input.split("\n")
    val parsedLines = lines.map { line ->
        val columns = line.split("\t")
        MatcherinoDonation(MatcherinoDonor(columns[0], columns.getOrNull(3)), columns[1].toDouble())
    }
    val groupedDonations =
        parsedLines.groupingBy { it.matcherinoDonor }
            .fold(0.toDouble()) { accumulator, element -> accumulator + element.amount }
    val donations = groupedDonations.map { entry -> MatcherinoDonation(entry.key, entry.value) }.sorted()
    println(donations)
    printMatcherino(donations, outputDiv)
}

fun printMatcherino(donations: List<MatcherinoDonation>, outputDiv: HTMLDivElement) {
    outputDiv.append.div {
        span("donor title") {
            img("tpotSellout", "https://static-cdn.jtvnw.net/emoticons/v1/469972/3.0", "titleImage")
            +"Donations"
            img("tpotSellout", "https://static-cdn.jtvnw.net/emoticons/v1/469972/3.0", "titleImage")
        }
        table {
            classes = setOf("totalDonations")
            donations.forEachIndexed { i, donation ->
                tr(classes = "matcherino" + if (i in 0..2) " matcherino$i" else if (donation.amount >= 100) " matcherino3" else "") {
//                    td {
//                        style = "text-align: right;"
//                        img(alt = "", src = donation.matcherinoDonor.avatar, classes = "itemIcon") {
//                            style = "vertical-align: middle; height: ${if (i <= 2) "2" else "1"}em; margin-right: 8px"
//                        }
//                    }
                    td {
                        style = "text-align: left;"
                        +donation.matcherinoDonor.name
                    }
                    td {
                        style = "text-align: right; vertical-align: middle;"
                        +"$${donation.amount.round(2)}"
                    }
                }
            }
        }
    }
}

fun printTotalDonation(totalDonations: List<Donation>, outputDiv: HTMLDivElement) {
    val totalValue = totalDonations.fold(0L) { accumulator, element -> accumulator + element.totalPrice }

    outputDiv.append.div {
        span("donor title") { +"Total donations" }
        table {
            classes = setOf("totalDonations")
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
                alt = donation.name
                src = donation.icon
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
            +donation.name
        }
        printPriceCell(donation.totalPrice)
    }
}

private fun TR.printPriceCell(totalPrice: Long) {
    td {
        style = "text-align: right; vertical-align: middle;"
        GoldDonation(totalPrice).printTotalAmount(this)
    }
}

fun printDonations(donors: List<Donor>, outputDiv: HTMLDivElement) {
    val bigDonors = donors.filter { it.isBigDonor }
    val smallDonors = donors.filter { !it.isBigDonor }
    outputDiv.append.div {
        bigDonors.forEach {
            renderDonor(it)
        }

        div("smallDonors") {
            smallDonors.forEach {
                div("smallDonor") {
                    renderDonor(it)
                }
            }
        }
    }

    initMagicGrid(smallDonors.size)
}

private fun initMagicGrid(count: Int) {
    MagicGrid(object : MagicGridProps {
        override var container = ".smallDonors"
        override var items: Number? = count
        override var gutter: Number? = 0
        override var animate: Boolean? = false
    }).listen()
}

private fun DIV.renderDonor(donor: Donor) = div("donor") {
    val itemsPerRow = if (donor.isBigDonor) ITEMS_PER_ROW else ITEMS_PER_ROW_SMALL

    span("donor title") {
        if (!donor.leftImage.isNullOrBlank()) {
            img("", donor.leftImage, "titleImage")
        }

        +donor.name

        if (!donor.rightImage.isNullOrBlank()) {
            img("", donor.rightImage, "titleImage")
        }
    }

    val itemRows = donor.donations.chunked(itemsPerRow)
    table("donationsTable") {
        itemRows.forEach { row ->
            if (row.size % 2 == itemsPerRow % 2) {
                renderItems(row, donor.isBigDonor, itemsPerRow)
            }
        }
    }

    val lastRow = itemRows.last()
    if (lastRow.size % 2 != itemsPerRow % 2) {
        table("donationsTable") { renderItems(lastRow, donor.isBigDonor, itemsPerRow) }
    }

    val totalValue = donor.donations.fold(0L) { accumulator, donation -> accumulator + donation.totalPrice }
    div("itemContainer totalValue") {
        +"Total value: "
        GoldDonation(totalValue).printTotalAmount(this)
    }
}

private fun TABLE.renderItems(items: List<Donation>, isBigDonor: Boolean, itemsPerRow: Int) = tr {
    val fakeTdCount = (itemsPerRow - items.size) / 2
    for (i in 0 until fakeTdCount) {
        td {}
    }
    items.forEach { renderItem(it, isBigDonor) }
    for (i in 0 until fakeTdCount) {
        td {}
    }
}

private fun TR.renderItem(donation: Donation, bigDonor: Boolean) = td {
    val imageSize = if (bigDonor) "64px" else "32px"
    div("itemContainer") {
        //    div(classes = "itemContainer" + if (center) " centerContent" else "") {
        img {
            alt = donation.name
            src = donation.icon
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

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}