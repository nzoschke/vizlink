package net.mixable.vizlink.data;

import java.util.ArrayList;
import org.deepsymmetry.beatlink.data.CueList;
import org.deepsymmetry.beatlink.data.SearchableItem;
import org.deepsymmetry.beatlink.data.TrackMetadata;

// see also reflect-config.json
public class Track {

  public String album;
  public String artist;
  public ArrayList<TrackCue> cues;
  public Long duration;
  public Integer player;
  public Source source;
  public Double tempo;
  public String title;
  public Integer year;

  public Track() {}

  public Track(Integer player, TrackMetadata tm) {
    this.player = player;

    album = label(tm.getAlbum());
    artist = label(tm.getArtist());
    duration = Long.valueOf(tm.getDuration()) * 1000;
    source = new Source(tm.trackReference);
    tempo = Double.valueOf(tm.getTempo()) / 100;
    title = tm.getTitle();
    year = tm.getYear();

    cues = new ArrayList<>();
    for (CueList.Entry e : tm.getCueList().entries) {
      cues.add(new TrackCue(e));
    }
  }

  private String label(SearchableItem i) {
    return i == null ? "" : i.label;
  }
}
