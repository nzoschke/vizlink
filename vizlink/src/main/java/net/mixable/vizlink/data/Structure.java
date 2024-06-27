package net.mixable.vizlink.data;

import io.kaitai.struct.ByteBufferKaitaiStream;
import java.util.ArrayList;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.SectionTags;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.SongStructureBody;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.SongStructureEntry;

// see also reflect-config.json
public class Structure {

  public String bank;
  public String mood;
  public ArrayList<StructurePhrase> phrases;
  public Integer player;

  public Structure() {}

  public Structure(Integer player, RekordboxAnlz.TaggedSection ts) {
    this.player = player;
    init(ts);
  }

  public Structure(byte[] bs) {
    RekordboxAnlz ext = new RekordboxAnlz(new ByteBufferKaitaiStream(bs));
    for (RekordboxAnlz.TaggedSection ts : ext.sections()) {
      if (ts.fourcc() == SectionTags.SONG_STRUCTURE) {
        init(ts);
        return;
      }
    }
  }

  public void init(RekordboxAnlz.TaggedSection ts) {
    RekordboxAnlz.SongStructureTag ss = new RekordboxAnlz.SongStructureTag(new ByteBufferKaitaiStream(ts._raw_body()));
    SongStructureBody b = ss.body();
    bank = b.bank().name().toLowerCase();
    mood = b.mood().name().toLowerCase();

    phrases = new ArrayList<>();

    ArrayList<SongStructureEntry> entries = ss.body().entries();
    for (int i = 0; i < entries.size(); i++) {
      RekordboxAnlz.SongStructureEntry e = entries.get(i);

      int endBeat = b.endBeat();
      if (i < entries.size() - 1) {
        endBeat = entries.get(i + 1).beat();
      }

      phrases.add(new StructurePhrase(e, endBeat));
    }
  }
}
