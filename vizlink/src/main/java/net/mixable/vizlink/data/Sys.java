package net.mixable.vizlink.data;

// see also reflect-config.json
public class Sys {

  public Integer code;
  public String msg;

  public Sys() {}

  public Sys(String msg, Integer code) {
    this.code = code;
    this.msg = msg;
  }
}
