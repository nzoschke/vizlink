package net.mixable.vizlink;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class IO {

  public final PipedInputStream in;
  private final PipedOutputStream inW;
  private final Consumer<String> out;

  public IO(Consumer<String> out) {
    in = new PipedInputStream();
    inW = new PipedOutputStream();
    try {
      inW.connect(in);
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.out = out;
  }

  public void writeIn(String s) {
    try {
      this.inW.write((s + "\n").getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void out(String s) {
    out.accept(s);
  }
}
