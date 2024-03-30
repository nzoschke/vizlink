package net.mixable.vizlink.data;

import java.util.ArrayList;

// see also reflect-config.json
public class RPC {

  public ArrayList<String> args;
  public String fn;

  public RPC() {}

  public RPC(ArrayList<String> args, String fn) {
    this.args = args;
    this.fn = fn;
  }
}
