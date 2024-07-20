package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.CdjStatus;

// see also reflect-config.json
public class Beat {

  public Integer beat;
  public Boolean master;
  public Boolean onAir;
  public Integer player;
  public Double tempo;

  public Beat() {}

  public Beat(CdjStatus s) {
    beat = s.getBeatNumber();
    master = s.isTempoMaster();
    onAir = s.isOnAir();
    player = s.getDeviceNumber();
    tempo = s.getEffectiveTempo();
  }
}
