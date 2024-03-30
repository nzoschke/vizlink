package net.mixable.vizlink.data;

// see also reflect-config.json
public class Beat {

  public Integer beat;
  public Boolean onAir;
  public Integer player;

  public Beat() {}

  public Beat(Integer beat, Boolean onAir, Integer player) {
    this.beat = beat;
    this.onAir = onAir;
    this.player = player;
  }
}
