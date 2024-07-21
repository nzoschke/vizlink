package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.CdjStatus;

// see also reflect-config.json
public class CDJ {

  public Integer barBeat;
  public Integer beat;
  public Boolean looping;
  public Integer masterPlayer;
  public Boolean master;
  public String name;
  public Boolean onAir;
  public Boolean paused;
  public Integer player;
  public Boolean playing;
  public Boolean sync;
  public Double tempo;
  public Source trackSource;

  public CDJ() {}

  public CDJ(CdjStatus s) {
    barBeat = s.getBeatWithinBar();
    beat = s.getBeatNumber();
    looping = s.isLooping();
    master = s.isTempoMaster();
    masterPlayer = s.getDeviceMasterIsBeingYieldedTo();
    name = s.getDeviceName();
    onAir = s.isOnAir();
    paused = s.isPaused();
    player = s.getDeviceNumber();
    playing = s.isPlaying();
    sync = s.isSynced();
    tempo = s.getEffectiveTempo();

    if (s.isTrackLoaded()) {
      trackSource = new Source(s);
    }
  }

  public CDJ(Boolean onAir, Integer player) {
    this.onAir = onAir;
    this.player = player;
  }
}
