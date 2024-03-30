package net.mixable.vizlink.data;

import java.util.Base64;
import org.deepsymmetry.beatlink.data.WaveformPreview;

// see also reflect-config.json
public class Waveform {

  public String data;
  public Integer player;

  public Waveform() {}

  public Waveform(Integer player, WaveformPreview wp) {
    this.data = new String(Base64.getEncoder().encode(wp.getData()).array());
    this.player = player;
  }
}
