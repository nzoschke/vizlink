package net.mixable.vizlink.data;

import java.util.Base64;
import org.deepsymmetry.beatlink.data.AlbumArt;

// see also reflect-config.json
public class Art {

  public String jpg;
  public Integer player;

  public Art() {}

  public Art(Integer player, AlbumArt aa) {
    this.player = player;
    this.jpg = new String(Base64.getEncoder().encode(aa.getRawBytes()).array());
  }
}
