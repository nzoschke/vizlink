package net.mixable.vizlink.data;

// see also reflect-config.json
public class Audio {

  public String dest;
  public Integer player;
  public String src;

  public Audio() {}

  public Audio(Integer player, String dest, String src) {
    this.player = player;
    this.dest = dest;
    this.src = src;
  }
}
