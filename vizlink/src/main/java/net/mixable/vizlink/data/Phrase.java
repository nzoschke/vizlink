package net.mixable.vizlink.data;

// see also reflect-config.json
public class Phrase {

  public Integer beat;
  public String kind;
  public Boolean onAir;
  public Integer player;

  public Phrase() {}

  public Phrase(Integer beat, String kind, Boolean onAir, Integer player) {
    this.beat = beat;
    this.kind = kind;
    this.onAir = onAir;
    this.player = player;
  }
}
