package vizlink

import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import vizlink.types.Message

class IO private constructor() {

  companion object {
    @Volatile private var instance: IO? = null

    fun getInstance() = instance ?: synchronized(this) { instance ?: IO().also { instance = it } }
  }

  inline fun <reified T> multicast(payload: T) {
    val serializer = serializer<T>()
    val msg =
      Message(
        Clock.System.now().toString(),
        Json.encodeToJsonElement(serializer, payload),
        T::class.java.simpleName.lowercase(),
        3,
      )
    println(Json.encodeToString(msg))
  }
}
