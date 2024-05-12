package net.mixable.vizlink;

import java.io.InputStream;
import java.util.function.Consumer;

public class IO {

  public InputStream in;
  private final Consumer<String> out;

  public IO(Consumer<String> out) {
    in = System.in;
    this.out = out;
  }

  public void write(String s) {
    out.accept(s);
  }
}
