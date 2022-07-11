import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import models.ExchangeResponse
import models.ItemDetails
import models.ItemPrice
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object Requests {
    private val json = Json { ignoreUnknownKeys = true }

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

        val parsed = json.decodeFromString(
            serializer,
            rawData
        )
        return parsed
    }

    suspend fun fetchItemDetails(ids: Iterable<Int>): List<ItemDetails> {
        return getBase(
            "https://api.guildwars2.com/v2/items?ids=${ids.joinToString(",")}",
            ListSerializer(ItemDetails.serializer())
        )
    }

    suspend fun fetchItemPrices(ids: Iterable<Int>): List<ItemPrice> {
        return getBase(
            "https://api.guildwars2.com/v2/commerce/prices?ids=${ids.joinToString(",")}",
            ListSerializer(ItemPrice.serializer())
        )
    }

    // rough estimate
    suspend fun fetchCoinsPerGem(): Long {
        val response = getBase(
            "https://api.guildwars2.com/v2/commerce/exchange/coins?quantity=36280000",
            ExchangeResponse.serializer()
        )
        return response.coinsPerGem
    }
}