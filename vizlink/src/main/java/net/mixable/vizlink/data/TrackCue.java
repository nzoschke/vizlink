package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.data.CueList;

// see also reflect-config.json
public class TrackCue {

  public String comment;
  public Long ms;

  public TrackCue() {}

  public TrackCue(CueList.Entry e) {
    comment = e.comment;
    ms = e.cueTime;
  }
}
