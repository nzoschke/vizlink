package net.mixable.vizlink.data;

import io.kaitai.struct.ByteBufferKaitaiStream;
import java.util.ArrayList;
import org.deepsymmetry.beatlink.data.BeatGrid;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.SectionTags;

// see also reflect-config.json
public class Grid {

  public ArrayList<GridBeat> beats;
  public Integer player;

  public Grid() {}

  public Grid(Integer player, BeatGrid bg) {
    this.player = player;

    beats = new ArrayList<>();
    for (int i = 1; i <= bg.beatCount; i++) {
      beats.add(new GridBeat(i, bg.getTimeWithinTrack(i)));
    }
  }

  public Grid(byte[] bs) {
    RekordboxAnlz ext = new RekordboxAnlz(new ByteBufferKaitaiStream(bs));
    for (RekordboxAnlz.TaggedSection ts : ext.sections()) {
      if (ts.fourcc() == SectionTags.BEAT_GRID) {
        init(ts);
        return;
      }
    }
  }

  public void init(RekordboxAnlz.TaggedSection ts) {
    RekordboxAnlz.BeatGridTag bg = new RekordboxAnlz.BeatGridTag(new ByteBufferKaitaiStream(ts._raw_body()));

    beats = new ArrayList<>();
    for (int beatNumber = 0; beatNumber < bg.numBeats(); beatNumber++) {
      RekordboxAnlz.BeatGridBeat beat = bg.beats().get(beatNumber);
      beats.add(new GridBeat(beatNumber, beat.time()));
    }
  }
}
