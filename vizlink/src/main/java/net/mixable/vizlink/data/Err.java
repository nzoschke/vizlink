package net.mixable.vizlink.data;

// see also reflect-config.json
public class Err {

  public Integer code;
  public String error;

  public Err() {}

  public Err(String error, Integer code) {
    this.code = code;
    this.error = error;
  }
}
