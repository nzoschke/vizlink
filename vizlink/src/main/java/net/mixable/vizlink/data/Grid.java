package net.mixable.vizlink.data;

import java.util.ArrayList;
import org.deepsymmetry.beatlink.data.BeatGrid;

// see also reflect-config.json
public class Grid {

  public ArrayList<GridBeat> beats;
  public Integer player;

  public Grid() {}

  public Grid(Integer player, BeatGrid bg) {
    this.player = player;

    beats = new ArrayList<>();
    for (int i = 1; i <= bg.beatCount; i++) {
      beats.add(new GridBeat(i, bg.getTimeWithinTrack(i)));
    }
  }
}
