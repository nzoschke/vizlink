package net.mixable.vizlink.data;

// see also reflect-config.json
public class Phrase {

  public String bank;
  public Integer beat;
  public String kind;
  public Boolean master;
  public String mood;
  public Boolean onAir;
  public Integer player;

  public Phrase() {}

  public Phrase(String bank, Integer beat, String kind, Boolean master, String mood, Boolean onAir, Integer player) {
    this.bank = bank;
    this.beat = beat;
    this.kind = kind;
    this.master = master;
    this.mood = mood;
    this.onAir = onAir;
    this.player = player;
  }
}
