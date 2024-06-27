package net.mixable.vizlink.data;

import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseHigh;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseLow;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.PhraseMid;
import org.deepsymmetry.cratedigger.pdb.RekordboxAnlz.SongStructureEntry;

// see also reflect-config.json
public class StructurePhrase {

  public Integer beat;
  public Integer beats;
  public String kind;

  public StructurePhrase() {}

  public StructurePhrase(SongStructureEntry e, int endBeat) {
    beat = e.beat();
    beats = endBeat - beat;

    if (e.kind() instanceof PhraseHigh) {
      kind = ((PhraseHigh) e.kind()).id().name().toLowerCase();
    }
    if (e.kind() instanceof PhraseMid) {
      kind = ((PhraseMid) e.kind()).id().name().toLowerCase();
    }
    if (e.kind() instanceof PhraseLow) {
      kind = ((PhraseLow) e.kind()).id().name().toLowerCase();
    }
  }
}
