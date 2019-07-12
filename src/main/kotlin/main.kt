import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

fun main() {
    val txtInput = document.getElementById("txtInput") as HTMLTextAreaElement
    val btnGo = document.getElementById("btnGo") as HTMLButtonElement
    btnGo.addEventListener("click", {
        parseInput(txtInput.value)
    })
}

private fun parseInput(input: String) {
    val lines = input.split("\n")
    val parsedLines = lines.map { line ->
        val columns = line.split("\t")
        ParsedLine(columns[0], columns[1].toInt(), columns[3].toInt())
    }
    val itemsIds = parsedLines.map { it.itemId }.toSet()
    println(itemsIds)
}