package net.mixable.vizlink;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.mixable.vizlink.data.Art;
import net.mixable.vizlink.data.CDJ;
import net.mixable.vizlink.data.Cue;
import net.mixable.vizlink.data.Device;
import net.mixable.vizlink.data.Err;
import net.mixable.vizlink.data.Grid;
import net.mixable.vizlink.data.Message;
import net.mixable.vizlink.data.Phrase;
import net.mixable.vizlink.data.RPC;
import net.mixable.vizlink.data.Structure;
import net.mixable.vizlink.data.StructurePhrase;
import net.mixable.vizlink.data.Sys;
import net.mixable.vizlink.data.Track;
import net.mixable.vizlink.data.Waveform;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.deepsymmetry.beatlink.Beat;
import org.deepsymmetry.beatlink.BeatFinder;
import org.deepsymmetry.beatlink.BeatListener;
import org.deepsymmetry.beatlink.CdjStatus;
import org.deepsymmetry.beatlink.DeviceAnnouncement;
import org.deepsymmetry.beatlink.DeviceAnnouncementListener;
import org.deepsymmetry.beatlink.DeviceFinder;
import org.deepsymmetry.beatlink.DeviceUpdate;
import org.deepsymmetry.beatlink.MediaDetails;
import org.deepsymmetry.beatlink.VirtualCdj;
import org.deepsymmetry.beatlink.data.AlbumArt;
import org.deepsymmetry.beatlink.data.AlbumArtListener;
import org.deepsymmetry.beatlink.data.AlbumArtUpdate;
import org.deepsymmetry.beatlink.data.AnalysisTagFinder;
import org.deepsymmetry.beatlink.data.AnalysisTagFinder.CacheEntry;
import org.deepsymmetry.beatlink.data.AnalysisTagListener;
import org.deepsymmetry.beatlink.data.AnalysisTagUpdate;
import org.deepsymmetry.beatlink.data.ArtFinder;
import org.deepsymmetry.beatlink.data.BeatGrid;
import org.deepsymmetry.beatlink.data.BeatGridFinder;
import org.deepsymmetry.beatlink.data.BeatGridListener;
import org.deepsymmetry.beatlink.data.BeatGridUpdate;
import org.deepsymmetry.beatlink.data.CrateDigger;
import org.deepsymmetry.beatlink.data.CueList;
import org.deepsymmetry.beatlink.data.DeckReference;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.TrackMetadata;
import org.deepsymmetry.beatlink.data.TrackMetadataListener;
import org.deepsymmetry.beatlink.data.TrackMetadataUpdate;
import org.deepsymmetry.beatlink.data.WaveformDetailUpdate;
import org.deepsymmetry.beatlink.data.WaveformFinder;
import org.deepsymmetry.beatlink.data.WaveformListener;
import org.deepsymmetry.beatlink.data.WaveformPreview;
import org.deepsymmetry.beatlink.data.WaveformPreviewUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  // maps of player number -> significant beats and status
  static Map<Integer, Map<Number, String>> beatCues = new ConcurrentHashMap<Integer, Map<Number, String>>();
  static Map<Integer, Map<Number, String>> beatPhrases = new ConcurrentHashMap<Integer, Map<Number, String>>();
  static Map<Integer, Boolean> onAirs = new ConcurrentHashMap<Integer, Boolean>();

  static Thread vcdjThread = null;

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    Options options = new Options();
    options.addOption("h", "help", false, "help");
    options.addOption("n", "number", true, "virtual CDJ player number (default 4)");
    options.addOption("r", "rpc", true, "RPC");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h")) {
      new HelpFormatter().printHelp("vizlink", options);
      return;
    }

    if (cmd.hasOption("r")) {
      rpc(cmd.getOptionValue("r"));
      System.exit(0);
    }

    int number = Integer.parseInt(coalesce(cmd.getOptionValue("n"), "4"));

    stdioListen();
    beatLinkListen();
    beatLinkStartDeviceFinder(number);

    while (true) {
      Thread.sleep(60000);
    }
  }

  public static void rpc(String payload) throws IOException {
    RPC r = OM.rpc(payload);
    if (r.fn.equals("structure")) {
      byte[] bs = Files.readAllBytes(new File(r.args.get(0)).toPath());
      Structure s = new Structure(bs);
      System.out.println(OM.string(new Message(s, "structure")));
    }
  }

  public static void stdioListen() {
    new Thread(() -> {
      System.err.println("stdioListen");
      System.out.println(OM.string(new Message(new Sys("hi", 0), "sys")));

      final Scanner scanner = new Scanner(System.in);
      while (true) {
        String line = scanner.nextLine();
        if (line.trim().equals("")) {
          continue;
        }

        Message m = OM.message(line);
        System.err.println(">" + OM.string(m));

        if (m.type.equals("sys")) {
          Sys sys = OM.sys(m);
          if (sys.msg.equals("echo")) {
            System.out.println(OM.string(new Message(sys, "sys")));
          }

          if (sys.msg.equals("exit")) {
            System.out.println(OM.string(new Message(new Sys("bye", 0), "sys")));
            scanner.close();
            System.exit(0);
          }

          if (sys.msg.equals("find")) {
            if (DeviceFinder.getInstance().isRunning()) {
              for (DeviceAnnouncement ann : DeviceFinder.getInstance().getCurrentDevices()) {
                DeviceUpdate du = VirtualCdj.getInstance().getLatestStatusFor(ann);
                if (!(du instanceof CdjStatus)) {
                  return;
                }

                CDJ p = new CDJ((CdjStatus) du);
                System.out.println(OM.string(new Message(p, "cdj")));
              }
            }

            int number = 0;
            if (VirtualCdj.getInstance().isRunning()) {
              number = VirtualCdj.getInstance().getDeviceNumber();
            }
            System.out.println(OM.string(new Message(new Sys("vcdj", number), "sys")));

            // emit track metadata before other data
            for (Map.Entry<DeckReference, TrackMetadata> entry : MetadataFinder.getInstance().getLoadedTracks().entrySet()) {
              DeckReference dr = entry.getKey();
              if (dr.hotCue != 0) continue;

              TrackMetadata metadata = entry.getValue();
              MediaDetails md = MetadataFinder.getInstance().getMediaDetailsFor(metadata.trackReference.getSlotReference());
              Track t = new Track(dr.player, metadata, md);
              System.out.println(OM.string(new Message(t, "track")));
            }

            for (Map.Entry<DeckReference, Map<String, CacheEntry>> entry : AnalysisTagFinder.getInstance().getLoadedAnalysisTags().entrySet()) {
              DeckReference dr = entry.getKey();
              if (dr.hotCue != 0) continue;
              CacheEntry ce = entry.getValue().get("PSSI.EXT");
              if (ce == null) continue;
              Structure s = new Structure(dr.player, ce.taggedSection);
              System.out.println(OM.string(new Message(s, "structure")));
            }

            for (Map.Entry<DeckReference, AlbumArt> entry : ArtFinder.getInstance().getLoadedArt().entrySet()) {
              DeckReference dr = entry.getKey();
              if (dr.hotCue != 0) continue;
              Art a = new Art(dr.player, entry.getValue());
              System.out.println(OM.string(new Message(a, "art")));
            }

            for (Map.Entry<DeckReference, BeatGrid> entry : BeatGridFinder.getInstance().getLoadedBeatGrids().entrySet()) {
              DeckReference dr = entry.getKey();
              if (dr.hotCue != 0) continue;
              Grid g = new Grid(dr.player, entry.getValue());
              System.out.println(OM.string(new Message(g, "grid")));
            }

            for (Map.Entry<DeckReference, WaveformPreview> entry : WaveformFinder.getInstance().getLoadedPreviews().entrySet()) {
              DeckReference dr = entry.getKey();
              if (dr.hotCue != 0) continue;
              Waveform w = new Waveform(dr.player, entry.getValue());
              System.out.println(OM.string(new Message(w, "waveform")));
            }
          }
        }

        if (m.type.equals("cdj")) {
          CDJ cdj = OM.cdj(m);
          if (cdj != null) {
            onAirUpdate(cdj.player, cdj.onAir);
          }
        }
      }
    }).start();
  }

  public static void beatLinkStartDeviceFinder(int number) {
    DeviceFinder df = DeviceFinder.getInstance();

    df.addDeviceAnnouncementListener(
      new DeviceAnnouncementListener() {
        @Override
        public void deviceFound(DeviceAnnouncement ann) {
          System.err.println("DeviceFinder.deviceFound: " + ann);
          if (vcdjThread == null) {
            vcdjStartWatchdog(number);
          }
        }

        @Override
        public void deviceLost(DeviceAnnouncement ann) {
          System.err.println("DeviceFinder.deviceLost: " + ann);
          if (df.getCurrentDevices().isEmpty()) {
            vcdjStopWatchdog();
          }
        }
      }
    );

    try {
      df.start();
      System.err.println("DeviceFinder.start");
    } catch (SocketException e) {
      System.out.println(OM.string(new Message(new Err("DeviceFinder: " + e.getMessage(), 99), "error")));
      System.exit(1);
    }
  }

  public static synchronized void vcdjStartWatchdog(int number) {
    vcdjThread = new Thread(() -> {
      while (true) {
        try {
          if (!VirtualCdj.getInstance().isRunning()) {
            vcdjStart(number);
          }
        } catch (Exception e) {
          System.out.println(OM.string(new Message(new Err("VirtualCdj: " + e.getMessage(), 99), "error")));
          e.printStackTrace();
        }

        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          // exit watchdog when interrupted by DeviceFinder.deviceLost last device
          System.err.println("VirtualCdj.interrupt");
          return;
        }
      }
    });

    vcdjThread.start();
  }

  public static synchronized void vcdjStopWatchdog() {
    VirtualCdj.getInstance().stop();
    System.err.println("VirtualCdj.stop");
    System.out.println(OM.string(new Message(new Sys("vcdj", 0), "sys")));

    if (vcdjThread != null) {
      vcdjThread.interrupt();
      vcdjThread = null;
    }
  }

  public static void vcdjStart(int number) throws Exception, SocketException {
    Boolean ok = VirtualCdj.getInstance().start((byte) (number));
    System.err.println("VirtualCdj.start: " + ok);

    if (ok) {
      System.out.println(OM.string(new Message(new Sys("vcdj", number), "sys")));

      for (DeviceAnnouncement ann : DeviceFinder.getInstance().getCurrentDevices()) {
        DeviceUpdate u = VirtualCdj.getInstance().getLatestStatusFor(ann);
        if (u instanceof CdjStatus) {
          CdjStatus s = (CdjStatus) u;
          onAirs.put(s.getDeviceNumber(), s.isOnAir());
        }
      }

      MetadataFinder.getInstance().setPassive(true);
      if (number >= 1 && number <= 4) {
        MetadataFinder.getInstance().setPassive(false);
      }

      AnalysisTagFinder.getInstance().start();
      ArtFinder.getInstance().setRequestHighResolutionArt(true);
      ArtFinder.getInstance().start();
      BeatFinder.getInstance().start();
      BeatGridFinder.getInstance().start();
      CrateDigger.getInstance().start();
      MetadataFinder.getInstance().start();
      WaveformFinder.getInstance().start();
    }
  }

  public static void beatLinkListen() {
    AnalysisTagFinder.getInstance()
      .addAnalysisTagListener(
        new AnalysisTagListener() {
          @Override
          public void analysisChanged(AnalysisTagUpdate update) {
            System.err.println("AnalysisTagFinder.analysisChanged: " + update);
            if (update.taggedSection == null) return;

            Structure s = new Structure(update.player, update.taggedSection);
            System.out.println(OM.string(new Message(s, "structure")));

            Map<Number, String> phrases = new ConcurrentHashMap<Number, String>();
            for (int i = 0; i < s.phrases.size(); i++) {
              StructurePhrase p = s.phrases.get(i);
              phrases.put(p.beat, p.kind);
            }

            beatPhrases.put(update.player, phrases);
          }
        },
        ".EXT",
        "PSSI"
      );

    ArtFinder.getInstance()
      .addAlbumArtListener(
        new AlbumArtListener() {
          @Override
          public void albumArtChanged(AlbumArtUpdate update) {
            System.err.println("ArtFinder.albumArtChanged: " + update);
            if (update.art == null) return;

            Art a = new Art(update.player, update.art);
            System.out.println(OM.string(new Message(a, "art")));
          }
        }
      );

    BeatFinder.getInstance()
      .addBeatListener(
        new BeatListener() {
          @Override
          public void newBeat(Beat beat) {
            DeviceUpdate du = VirtualCdj.getInstance().getLatestStatusFor(beat);
            if (!(du instanceof CdjStatus)) {
              return;
            }

            CdjStatus s = (CdjStatus) du;
            CDJ cdj = new CDJ(s);

            onAirUpdate(cdj.player, cdj.onAir);

            // publish beats that are downbeat, cues or phrases
            if (beat.getBeatWithinBar() == 1) {
              System.err.println("BeatFinder.newBeat: " + beat);
              System.err.println("BeatFinder.cdjStatus: " + s);

              net.mixable.vizlink.data.Beat b = new net.mixable.vizlink.data.Beat(cdj.beat, cdj.onAir, cdj.player);
              System.out.println(OM.string(new Message(b, "beat")));
            }

            Map<Number, String> cues = beatCues.get(cdj.player);
            if (cues != null && cues.containsKey(cdj.beat)) {
              Cue c = new Cue(cdj.beat, cues.get(cdj.beat), cdj.onAir, cdj.player);
              System.out.println(OM.string(new Message(c, "cue")));
            }

            Map<Number, String> phrases = beatPhrases.get(cdj.player);
            if (phrases != null && phrases.containsKey(cdj.beat)) {
              Phrase p = new Phrase(cdj.beat, phrases.get(cdj.beat), cdj.onAir, cdj.player);
              System.out.println(OM.string(new Message(p, "phrase")));
            }
          }
        }
      );

    BeatGridFinder.getInstance()
      .addBeatGridListener(
        new BeatGridListener() {
          @Override
          public void beatGridChanged(BeatGridUpdate update) {
            System.err.println("BeatGridFinder.beatGridChanged: " + update);

            if (update.beatGrid == null) return;

            BeatGrid bg = update.beatGrid;
            Grid g = new Grid(update.player, bg);
            System.out.println(OM.string(new Message(g, "grid")));

            TrackMetadata tm = MetadataFinder.getInstance().getLatestMetadataFor(update.player);
            if (tm == null) return;

            Map<Number, String> cues = new ConcurrentHashMap<Number, String>();
            for (CueList.Entry e : tm.getCueList().entries) {
              cues.put(bg.findBeatAtTime(e.cueTime), e.comment);
            }

            beatCues.put(update.player, cues);
          }
        }
      );

    DeviceFinder.getInstance()
      .addDeviceAnnouncementListener(
        new DeviceAnnouncementListener() {
          @Override
          public void deviceFound(DeviceAnnouncement ann) {
            System.err.println("DeviceFinder.deviceFound: " + ann);

            Device d = new Device(ann, true);
            System.out.println(OM.string(new Message(d, "device")));
          }

          @Override
          public void deviceLost(DeviceAnnouncement ann) {
            System.err.println("DeviceFinder.deviceLost: " + ann);

            Device d = new Device(ann, false);
            System.out.println(OM.string(new Message(d, "device")));
          }
        }
      );

    MetadataFinder.getInstance()
      .addTrackMetadataListener(
        new TrackMetadataListener() {
          @Override
          public void metadataChanged(TrackMetadataUpdate update) {
            System.err.println("MetadataFinder.metadataChanged: " + update);
            if (update.metadata == null) return;

            MediaDetails md = MetadataFinder.getInstance().getMediaDetailsFor(update.metadata.trackReference.getSlotReference());
            Track t = new Track(update.player, update.metadata, md);
            System.out.println(OM.string(new Message(t, "track")));
          }
        }
      );

    WaveformFinder.getInstance()
      .addWaveformListener(
        new WaveformListener() {
          @Override
          public void previewChanged(WaveformPreviewUpdate update) {
            System.err.println("WaveformFinder.previewChanged: " + update);
            update.preview.getData();

            Waveform w = new Waveform(update.player, update.preview);
            System.out.println(OM.string(new Message(w, "waveform")));
          }

          @Override
          public void detailChanged(WaveformDetailUpdate update) {
            System.err.println("WaveformFinder.detailChanged: " + update);
          }
        }
      );
  }

  public static synchronized void onAirUpdate(Integer player, Boolean onAir) {
    if (onAirs.get(player) == onAir) {
      return;
    }

    // echo back the command for testing purposes
    if (!VirtualCdj.getInstance().isRunning()) {
      CDJ cdj = new CDJ(onAir, player);
      System.out.println(OM.string(new Message(cdj, "cdj")));
      return;
    }

    Set<Integer> current = new HashSet<>();
    for (Map.Entry<Integer, Boolean> entry : onAirs.entrySet()) {
      if (entry.getValue()) {
        current.add(entry.getKey());
      }
    }

    Set<Integer> next = new HashSet<>(current);
    next.remove(player);
    if (onAir) {
      next.add(player);
    }

    if (!current.equals(next)) {
      onAirs.put(player, onAir);

      System.err.println("VirtualCdj.sendOnAirExtendedCommand: " + next);
      try {
        VirtualCdj.getInstance().sendOnAirExtendedCommand(next);
        DeviceUpdate du = VirtualCdj.getInstance().getLatestStatusFor(player);
        CDJ cdj = new CDJ((CdjStatus) du);
        cdj.onAir = onAir;
        System.out.println(OM.string(new Message(cdj, "cdj")));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String coalesce(String val, String def) {
    if (val == null || val == "") {
      return def;
    }
    return val;
  }
}
