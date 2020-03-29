import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import models.ItemDetails
import models.ItemPrice
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object Requests {
    private fun statusHandler(xhr: XMLHttpRequest, coroutineContext: Continuation<String>) {
        if (xhr.readyState == XMLHttpRequest.DONE) {
            if (xhr.status / 100 == 2) {
                coroutineContext.resume(xhr.response as String)
            } else {
                coroutineContext.resumeWithException(RuntimeException("HTTP error: ${xhr.status}"))
            }
        }
    }

    private suspend fun httpGet(url: String): String = suspendCoroutine { c ->
        val xhr = XMLHttpRequest()
        xhr.onreadystatechange = { _ -> statusHandler(xhr, c) }
        xhr.open("GET", url)
        xhr.send()
    }

    private suspend inline fun <reified R : Any> getBase(url: String, serializer: KSerializer<R>): R {
        val rawData = httpGet(url)
        console.log(rawData)
        val parsed = Json(JsonConfiguration.Default.copy(ignoreUnknownKeys = true)).parse(serializer, rawData)
        return parsed
    }

    suspend fun fetchItemDetails(ids: Iterable<Int>): List<ItemDetails> {
        return getBase(
            "https://api.guildwars2.com/v2/items?ids=${ids.joinToString(",")}",
            ItemDetails.serializer().list
        )
    }

    suspend fun fetchItemPrices(ids: Iterable<Int>): List<ItemPrice> {
        return getBase(
            "http://api.guildwars2.com/v2/commerce/prices?ids=${ids.joinToString(",")}",
            ItemPrice.serializer().list
        )
    }
}