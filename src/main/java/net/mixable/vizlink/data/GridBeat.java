package net.mixable.vizlink.data;

// see also reflect-config.json
public class GridBeat {

  public Integer beat;
  public Long ms;

  public GridBeat() {}

  public GridBeat(Integer beat, Long ms) {
    this.beat = beat;
    this.ms = ms;
  }
}
