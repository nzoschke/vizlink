package net.mixable.vizlink.data;

// see also reflect-config.json
public class Cue {

  public Integer beat;
  public String comment;
  public Boolean onAir;
  public Integer player;

  public Cue() {}

  public Cue(Integer beat, String comment, Boolean onAir, Integer player) {
    this.beat = beat;
    this.comment = comment;
    this.onAir = onAir;
    this.player = player;
  }
}
