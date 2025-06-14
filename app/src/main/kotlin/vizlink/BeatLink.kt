package vizlink

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.nio.file.Paths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.deepsymmetry.beatlink.Beat
import org.deepsymmetry.beatlink.BeatFinder
import org.deepsymmetry.beatlink.BeatListener
import org.deepsymmetry.beatlink.CdjStatus
import org.deepsymmetry.beatlink.CdjStatus.TrackSourceSlot
import org.deepsymmetry.beatlink.DeviceAnnouncement
import org.deepsymmetry.beatlink.DeviceAnnouncementListener
import org.deepsymmetry.beatlink.DeviceFinder
import org.deepsymmetry.beatlink.DeviceUpdate
import org.deepsymmetry.beatlink.MasterListener
import org.deepsymmetry.beatlink.MediaDetails
import org.deepsymmetry.beatlink.OnAirListener
import org.deepsymmetry.beatlink.Util
import org.deepsymmetry.beatlink.VirtualCdj
import org.deepsymmetry.beatlink.data.AlbumArtListener
import org.deepsymmetry.beatlink.data.AlbumArtUpdate
import org.deepsymmetry.beatlink.data.AnalysisTagFinder
import org.deepsymmetry.beatlink.data.AnalysisTagListener
import org.deepsymmetry.beatlink.data.AnalysisTagUpdate
import org.deepsymmetry.beatlink.data.ArtFinder
import org.deepsymmetry.beatlink.data.BeatGridFinder
import org.deepsymmetry.beatlink.data.BeatGridListener
import org.deepsymmetry.beatlink.data.BeatGridUpdate
import org.deepsymmetry.beatlink.data.CrateDigger
import org.deepsymmetry.beatlink.data.DataReference
import org.deepsymmetry.beatlink.data.MetadataFinder
import org.deepsymmetry.beatlink.data.MountListener
import org.deepsymmetry.beatlink.data.SlotReference
import org.deepsymmetry.beatlink.data.TimeFinder
import org.deepsymmetry.beatlink.data.TrackMetadata
import org.deepsymmetry.beatlink.data.TrackMetadataListener
import org.deepsymmetry.beatlink.data.TrackMetadataUpdate
import org.deepsymmetry.beatlink.data.WaveformDetailUpdate
import org.deepsymmetry.beatlink.data.WaveformFinder
import org.deepsymmetry.beatlink.data.WaveformListener
import org.deepsymmetry.beatlink.data.WaveformPreviewUpdate
import org.deepsymmetry.cratedigger.Database
import org.jaudiotagger.audio.AudioFileIO
import vizlink.types.*

class BeatLink private constructor() {
  val io = IO.getInstance()
  val tag = "BeatLink"

  private val atf = AnalysisTagFinder.getInstance()
  private val af = ArtFinder.getInstance()
  private val bgf = BeatGridFinder.getInstance()
  private val df = DeviceFinder.getInstance()
  private val ff = FileFetcher.getInstance()
  private val mf = MetadataFinder.getInstance()
  private val tf = TimeFinder.getInstance()
  private val wf = WaveformFinder.getInstance()
  private val vcdj = VirtualCdj.getInstance()

  var master = 0
  var onAirs = mutableSetOf<Int>()

  companion object {
    @Volatile private var instance: BeatLink? = null

    fun getInstance() =
      instance ?: synchronized(this) { instance ?: BeatLink().also { instance = it } }
  }

  fun cover(tm: TrackMetadata, c: CdjStatus, m: MediaDetails) {
    CoroutineScope(Dispatchers.IO).launch {
      val path = fetch(tm)
      val af = AudioFileIO.read(File(path))
      io.multicast(cover(af.tag.firstArtwork.binaryData, c, m))
    }
  }

  private fun fetch(tm: TrackMetadata): String {
    val dr: DataReference = tm.trackReference
    val player = df.getLatestAnnouncementFrom(dr.player)

    var mountPath = "/B/" // SD_SLOT
    if (dr.slot == TrackSourceSlot.USB_SLOT) {
      mountPath = "/C/"
    }

    val db = CrateDigger.getInstance().findDatabase(tm.trackReference)
    val tr = db.trackIndex[tm.trackReference.rekordboxId.toLong()]
    val src = Database.getText(tr!!.filePath())

    val dir = Paths.get(System.getProperty("user.home"), "Music", "VizLab", "Cache").toString()
    File(dir).mkdirs()

    val dest = File(dir, File(src).name)
    val part = File(dest.absolutePath + ".tmp")

    if (!dest.exists()) {
      try {
        ff.fetch(player.address, mountPath, src, part, 2048 * 1024)
      } catch (e: IOException) {
        e.printStackTrace()
        return ""
      }

      part.renameTo(dest)
    }

    return dest.absolutePath
  }

  private fun info() {
    Log.v(tag, "info")
    if (!df.isRunning || !vcdj.isRunning) return

    for (ann in df.currentDevices) {
      val (c, m) = status(ann.deviceNumber) ?: continue
      io.multicast(cdj(c, m))
    }

    for (t in mf.loadedTracks) {
      if (t.key.hotCue != 0) continue
      val (c, m) = status(t.key.player) ?: continue
      io.multicast(track(t.value, c, m))
      cover(t.value, c, m)
    }

    for (t in atf.loadedAnalysisTags) {
      if (t.key.hotCue != 0) continue
      val ce = t.value["PSSI.EXT"] ?: continue
      val (c, m) = status(t.key.player) ?: continue
      io.multicast(structure(ce.taggedSection, c, m))
    }

    for (t in af.loadedArt) {
      if (t.key.hotCue != 0) continue
      val (c, m) = status(t.key.player) ?: continue
      io.multicast(art(t.value, c, m))
    }

    for (t in bgf.loadedBeatGrids) {
      if (t.key.hotCue != 0) continue
      val (c, m) = status(t.key.player) ?: continue
      io.multicast(grid(t.value, c, m))
    }

    for (t in wf.loadedPreviews) {
      if (t.key.hotCue != 0) continue
      val (c, m) = status(t.key.player) ?: continue
      io.multicast(waveform(t.value, c, m))
    }
  }

  fun start() {
    stdin()

    startDF()
    startATF()
    startAF()
    startBF()
    startBGF()
    startCD()
    startMF()
    startTF()
    startWF()
  }

  fun status(player: Int): Pair<CdjStatus, MediaDetails>? {
    val s = vcdj.getLatestStatusFor(player)
    if (s == null || s !is CdjStatus) return null

    val tm = mf.getLatestMetadataFor(player) ?: return null
    val m = mf.getMediaDetailsFor(tm.trackReference.slotReference)
    return Pair(s, m)
  }

  private fun startATF() {
    atf.start()
    atf.addAnalysisTagListener(
      object : AnalysisTagListener {
        override fun analysisChanged(u: AnalysisTagUpdate) {
          Log.v(tag, "analysisChanged:$u")
          if (u.taggedSection == null) return
          val (c, m) = status(u.player) ?: return
          io.multicast(structure(u.taggedSection, c, m))
        }
      },
      ".EXT",
      "PSSI",
    )
  }

  private fun startAF() {
    af.requestHighResolutionArt = true
    af.start()
    af.addAlbumArtListener(
      object : AlbumArtListener {
        override fun albumArtChanged(u: AlbumArtUpdate) {
          Log.v(tag, "albumArtChanged:$u")
          val (c, m) = status(u.player) ?: return
          io.multicast(art(u.art, c, m))
        }
      }
    )
  }

  private fun startBF() {
    val f = BeatFinder.getInstance()
    f.start()
    f.addBeatListener(
      object : BeatListener {
        override fun newBeat(beat: Beat) {
          if (beat.beatWithinBar != 1) return
          Log.v(tag, "newBeat:$beat")

          val (c, m) = status(beat.deviceNumber) ?: return
          io.multicast(beat(c, m))

          // TODO: Cue and Phrase
        }
      }
    )

    f.addOnAirListener(
      object : OnAirListener {
        override fun channelsOnAir(audibleChannels: MutableSet<Int>) {
          if (onAirs == audibleChannels) return
          Log.v(tag, "channelsOnAir:$audibleChannels")

          onAirs = audibleChannels

          // synthesize latest onAir status for all players
          for (ann in df.currentDevices) {
            val (c, m) = status(ann.deviceNumber) ?: continue
            io.multicast(cdj(c, m, null, onAirs.contains(c.deviceNumber)))
          }
        }
      }
    )
  }

  private fun startBGF() {
    bgf.start()
    bgf.addBeatGridListener(
      object : BeatGridListener {
        override fun beatGridChanged(u: BeatGridUpdate) {
          Log.v(tag, "beatGridChanged:$u")
          if (u.beatGrid == null) return

          val (c, m) = status(u.player) ?: return
          io.multicast(grid(u.beatGrid, c, m))
        }
      }
    )
  }

  private fun startCD() {
    val f = CrateDigger.getInstance()
    f.start()
  }

  private fun startDF() {
    df.start()

    df.addDeviceAnnouncementListener(
      object : DeviceAnnouncementListener {
        override fun deviceFound(a: DeviceAnnouncement) {
          Log.v(tag, "deviceFound:$a")

          if (!vcdj.isRunning) {
            startVCDJ()
          }

          io.multicast(device(true, a))
        }

        override fun deviceLost(a: DeviceAnnouncement) {
          Log.v(tag, "deviceLost:$a")

          if (df.currentDevices.isEmpty()) {
            vcdj.stop()
          }

          io.multicast(device(false, a))
        }
      }
    )
  }

  private fun startMF() {
    mf.isPassive = true
    mf.start()

    mf.addMountListener(
      object : MountListener {
        override fun mediaMounted(slot: SlotReference) {
          Log.v(tag, "mediaMounted:$slot")
        }

        override fun mediaUnmounted(slot: SlotReference) {
          Log.v(tag, "mediaUnmounted:$slot")
        }
      }
    )

    mf.addTrackMetadataListener(
      object : TrackMetadataListener {
        override fun metadataChanged(u: TrackMetadataUpdate) {
          Log.v(tag, "metadataChanged:$u")
          val (c, m) = status(u.player) ?: return
          io.multicast(track(u.metadata, c, m))
          io.multicast(cdj(c, m))
          cover(u.metadata, c, m)
        }
      }
    )
  }

  private fun startTF() {
    tf.start()
  }

  private fun startVCDJ() {
    vcdj.setDeviceName("vizlink")
    vcdj.start(7)

    val interfaces =
      NetworkInterface.getNetworkInterfaces()
        .asSequence()
        .mapNotNull { Util.findMatchingAddress(df.currentDevices.first(), it) }
        .toList()

    val log =
      when {
        interfaces.isEmpty() -> Log("error", "No network interfaces")
        interfaces.size == 1 -> Log("success", "One network interface")
        else -> Log("warning", "Multiple network interfaces")
      }
    io.multicast(log)

    vcdj.addMasterListener(
      object : MasterListener {
        override fun newBeat(beat: Beat) {
          // Log.v(tag, "newBeat:$beat")
        }

        override fun masterChanged(update: DeviceUpdate) {
          if (update.deviceNumber == master) return
          Log.v(tag, "masterChanged:$update")

          master = update.deviceNumber

          // synthesize latest master for all players
          for (ann in df.currentDevices) {
            val (c, m) = status(ann.deviceNumber) ?: continue
            io.multicast(cdj(c, m, master == c.deviceNumber, null))
          }
        }

        override fun tempoChanged(tempo: Double) {
          // Log.v(tag, "tempoChanged:$tempo")
        }
      }
    )
  }

  private fun startWF() {
    val f = WaveformFinder.getInstance()
    f.start()

    f.addWaveformListener(
      object : WaveformListener {
        override fun previewChanged(u: WaveformPreviewUpdate) {
          Log.v(tag, "previewChanged:$u")
          val (c, m) = status(u.player) ?: return
          io.multicast(waveform(u.preview, c, m))
        }

        override fun detailChanged(u: WaveformDetailUpdate) {
          // Log.v(tag, "detailChanged:u")
        }
      }
    )
  }

  private fun stdin() {
    CoroutineScope(Dispatchers.IO).launch {
      val reader = BufferedReader(InputStreamReader(System.`in`))
      val json = Json { ignoreUnknownKeys = true }
      reader.use {
        while (true) {
          val line = it.readLine() ?: break
          Log.v(tag, "stdin:$line")
          try {
            val cmd = json.decodeFromString<Command>(line)
            if (cmd.type == "info") info()
          } catch (e: Exception) {
            // ignore decode errors
          }
        }
      }
    }
  }
}
