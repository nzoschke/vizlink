package net.mixable.vizlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IO {

  public InputStream in;
  public OutputStream out;

  public IO() {
    in = System.in;
    out = System.out;
  }

  public void write(String s) {
    try {
      out.write((s + "\n").getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
