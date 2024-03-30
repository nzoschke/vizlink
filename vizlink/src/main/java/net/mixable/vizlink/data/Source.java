package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.CdjStatus;
import org.deepsymmetry.beatlink.data.DataReference;

// see also reflect-config.json
public class Source {

  public Integer id;
  public Integer player;
  public String slot;

  public Source() {}

  public Source(Integer id, Integer player, String slot) {
    this.id = id;
    this.player = player;
    this.slot = slot;
  }

  public Source(CdjStatus s) {
    id = s.getRekordboxId();
    player = s.getTrackSourcePlayer();
    slot = s.getTrackSourceSlot().name();
  }

  public Source(DataReference d) {
    id = d.rekordboxId;
    player = d.player;
    slot = d.slot.name();
  }
}
