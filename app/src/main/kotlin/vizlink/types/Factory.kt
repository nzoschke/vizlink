package vizlink.types

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.deepsymmetry.beatlink.CdjStatus
import org.deepsymmetry.beatlink.DeviceAnnouncement
import org.deepsymmetry.beatlink.MediaDetails
import org.deepsymmetry.beatlink.data.AlbumArt
import org.deepsymmetry.beatlink.data.BeatGrid
import org.deepsymmetry.beatlink.data.DataReference
import org.deepsymmetry.beatlink.data.TrackMetadata
import org.deepsymmetry.beatlink.data.WaveformPreview
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseHigh
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseLow
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseMid

// fun art(a: AlbumArt, c: CdjStatus, m: MediaDetails): Art = art(a.rawBytes.array(), c, m)

@OptIn(ExperimentalEncodingApi::class)
fun art(a: AlbumArt, c: CdjStatus, m: MediaDetails): Art =
  Art(Base64.encode(a.rawBytes.array()), c.deviceNumber.toLong(), source(c, m))

fun beat(c: CdjStatus, m: MediaDetails): Beat {
  return Beat(
    c.beatNumber.toLong(),
    c.isTempoMaster,
    c.isOnAir,
    c.deviceNumber.toLong(),
    source(c, m),
    c.effectiveTempo,
  )
}

fun cdj(c: CdjStatus, m: MediaDetails): Cdj {
  var deviceMaster = 0L

  if (c.deviceMasterIsBeingYieldedTo != null) {
    deviceMaster = c.deviceMasterIsBeingYieldedTo.toLong()
  }

  return Cdj(
    c.beatWithinBar.toLong(),
    c.beatNumber.toLong(),
    c.isLooping,
    c.isTempoMaster,
    deviceMaster,
    c.deviceName,
    c.isOnAir,
    c.isPaused,
    c.deviceNumber.toLong(),
    c.isPlaying,
    source(c, m),
    c.isSynced,
    c.effectiveTempo,
  )
}

fun cdj(u: CdjStatus, m: MediaDetails, master: Boolean?, onAir: Boolean?): Cdj {
  val p = cdj(u, m)

  return Cdj(
    p.barBeat,
    p.beat,
    p.looping,
    master ?: p.master,
    p.masterPlayer,
    p.name,
    onAir ?: p.onAir,
    p.paused,
    p.player,
    p.playing,
    p.src,
    p.sync,
    p.tempo,
  )
}

@OptIn(ExperimentalEncodingApi::class)
fun cover(bs: ByteArray, c: CdjStatus, m: MediaDetails): Cover =
  Cover(Base64.encode(bs), c.deviceNumber.toLong(), source(c, m))

fun device(active: Boolean, a: DeviceAnnouncement): Device {
  return Device(active, a.deviceName, a.deviceNumber.toLong())
}

fun grid(b: BeatGrid, c: CdjStatus, m: MediaDetails): Grid {
  val beats = mutableListOf<GridBeat>()
  for (i in 1..b.beatCount) {
    beats.add(GridBeat(i.toLong(), b.getTimeWithinTrack(i)))
  }

  return Grid(beats, c.deviceNumber.toLong(), source(c, m))
}

fun source(d: DataReference, m: MediaDetails): String {
  return "${m.name}/${d.rekordboxId}" // "GREP stealth/6672",
}

fun source(c: CdjStatus, m: MediaDetails): String {
  return "${m.name}/${c.rekordboxId}" // "GREP stealth/6672",
}

fun structure(ts: RekordboxAnlz.TaggedSection, c: CdjStatus, m: MediaDetails): Structure {
  val body = (ts.body() as RekordboxAnlz.SongStructureTag).body()

  val entries = body.entries()
  val phrases = mutableListOf<Phrase>()
  for ((i, e) in entries.withIndex()) {
    var endBeat = body.endBeat()
    if (i < entries.size - 1) {
      endBeat = entries[i + 1].beat()
    }

    var kind = ""
    if (e.kind() is PhraseHigh) {
      kind = (e.kind() as PhraseHigh).id().name.lowercase()
    }

    if (e.kind() is PhraseMid) {
      kind = (e.kind() as PhraseMid).id().name.lowercase()
    }

    if (e.kind() is PhraseLow) {
      kind = (e.kind() as PhraseLow).id().name.lowercase()
    }

    phrases.add(Phrase(e.beat().toLong(), (endBeat - e.beat()).toLong(), kind))
  }

  return Structure(
    body.bank().name.lowercase(),
    body.mood().name.lowercase(),
    phrases,
    c.deviceNumber.toLong(),
    source(c, m),
  )
}

fun track(md: TrackMetadata, c: CdjStatus, m: MediaDetails): Track {
  var album = ""
  var originalArtist = ""
  var genre = ""

  if (md.album != null) {
    album = md.album.label
  }

  if (md.genre != null) {
    genre = md.genre.label
  }

  if (md.originalArtist != null) {
    originalArtist = md.originalArtist.label
  }

  return Track(
    album,
    originalArtist,
    md.artist.label,
    md.tempo / 100.0,
    md.comment,
    c.deviceNumber.toLong(),
    genre,
    "",
    md.key.label,
    md.duration * 1000L,
    "",
    source(md.trackReference, m),
    md.title,
    0L,
    md.trackType.name.lowercase(),
    md.year.toString(),
  )
}

fun waveform(w: WaveformPreview, c: CdjStatus, m: MediaDetails): Waveform {
  val back = mutableListOf<Segment>()
  val front = mutableListOf<Segment>()

  for (i in 0..<w.segmentCount) {
    val cb = w.segmentColor(i, false)
    val hb = w.segmentHeight(i, false)
    back.add(Segment(cb.red.toLong(), cb.green.toLong(), cb.blue.toLong(), hb.toLong()))

    val cf = w.segmentColor(i, true)
    val hf = w.segmentHeight(i, true)
    front.add(Segment(cf.red.toLong(), cf.green.toLong(), cf.blue.toLong(), hf.toLong()))
  }

  return Waveform(back, front, c.deviceNumber.toLong(), source(c, m))
}
