package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.data.TrackMetadata;

// see also reflect-config.json
public class Audio {

  public String dest;
  public Integer player;
  public String src;
  public Source source;

  public Audio() {}

  public Audio(Integer player, TrackMetadata tm, String dest, String src) {
    this.player = player;
    this.dest = dest;
    this.src = src;
    source = new Source(tm.trackReference);
  }
}
