package net.mixable.vizlink.data;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

// see also reflect-config.json
public class Audio {

  public String bytes;
  public String name;
  public Integer player;

  public Audio() {}

  public Audio(Integer player, java.io.File f, String name) {
    try {
      this.bytes = new String(Base64.getEncoder().encodeToString(Files.readAllBytes(f.toPath())));
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.player = player;
    this.name = name;
  }
}
