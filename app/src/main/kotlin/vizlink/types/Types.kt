// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json       = Json { allowStructuredMapKeys = true }
// val art        = json.parse(Art.serializer(), jsonString)
// val beat       = json.parse(Beat.serializer(), jsonString)
// val command    = json.parse(Command.serializer(), jsonString)
// val cover      = json.parse(Cover.serializer(), jsonString)
// val cdj        = json.parse(Cdj.serializer(), jsonString)
// val device     = json.parse(Device.serializer(), jsonString)
// val log        = json.parse(Log.serializer(), jsonString)
// val message    = json.parse(Message.serializer(), jsonString)
// val grid       = json.parse(Grid.serializer(), jsonString)
// val gridBeat   = json.parse(GridBeat.serializer(), jsonString)
// val segment    = json.parse(Segment.serializer(), jsonString)
// val structure  = json.parse(Structure.serializer(), jsonString)
// val track      = json.parse(Track.serializer(), jsonString)
// val phrase     = json.parse(Phrase.serializer(), jsonString)
// val cue        = json.parse(Cue.serializer(), jsonString)
// val waveform   = json.parse(Waveform.serializer(), jsonString)
// val oSCMessage = json.parse(OSCMessage.serializer(), jsonString)

package vizlink.types

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable data class Art(val jpg: String, val player: Long, val src: String)

@Serializable
data class Beat(
  val beat: Long,
  val master: Boolean,
  val onAir: Boolean,
  val player: Long,
  val src: String,
  val tempo: Double,
)

@Serializable data class Command(val type: String)

@Serializable data class Cover(val jpg: String, val player: Long, val src: String)

@Serializable
data class Cdj(
  val barBeat: Long,
  val beat: Long,
  val looping: Boolean,
  val master: Boolean,
  val masterPlayer: Long,
  val name: String,
  val onAir: Boolean,
  val paused: Boolean,
  val player: Long,
  val playing: Boolean,
  val src: String,
  val sync: Boolean,
  val tempo: Double,
)

@Serializable data class Device(val active: Boolean, val name: String, val player: Long)

@Serializable data class Log(val level: String, val msg: String)

@Serializable
data class Message(val at: String, val payload: JsonElement?, val type: String, val version: Long)

@Serializable data class Grid(val beats: List<GridBeat>, val player: Long, val src: String)

@Serializable data class GridBeat(val beat: Long, val ms: Long)

@Serializable
data class Structure(
  val bank: String,
  val mood: String,
  val phrases: List<Phrase>,
  val player: Long,
  val src: String,
)

@Serializable data class Phrase(val beat: Long, val beats: Long, val kind: String)

@Serializable
data class Track(
  val album: String,
  val albumArtist: String,
  val artist: String,
  val bpm: Double,
  val comment: String,
  val disc: Long,
  val genre: String,
  val isrc: String,
  val key: String,
  val length: Long,
  val mood: String,
  val src: String,
  val title: String,
  val track: Long,
  val type: String,
  val year: String,
)

@Serializable data class Cue(val comment: String, val ms: Long)

@Serializable
data class Waveform(
  val back: List<Segment>,
  val front: List<Segment>,
  val player: Long,
  val src: String,
)

@Serializable data class Segment(val b: Long, val g: Long, val h: Long, val r: Long)

@Serializable data class OSCMessage(val address: String, val args: JsonArray)
