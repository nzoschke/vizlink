package net.mixable.vizlink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.mixable.vizlink.data.CDJ;
import net.mixable.vizlink.data.Err;
import net.mixable.vizlink.data.Message;
import net.mixable.vizlink.data.RPC;
import net.mixable.vizlink.data.Sys;

public class OM {

  public static final ObjectMapper om = new ObjectMapper();

  private OM() {}

  public static Message message(String json) {
    try {
      return om.readValue(json, Message.class);
    } catch (JsonProcessingException e) {
      return new Message(new Err(e.getMessage(), null), "error");
    }
  }

  public static CDJ cdj(Message m) {
    try {
      return om.readValue(string(m.payload), CDJ.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static RPC rpc(String json) {
    try {
      return om.readValue(json, RPC.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Sys sys(Message m) {
    try {
      return om.readValue(string(m.payload), Sys.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String string(Object value) {
    try {
      return om.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return error(e.getMessage(), null);
    }
  }

  public static String error(String error, Integer code) {
    try {
      return om.writeValueAsString(new Err(error, code));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{\"error\":\"unknown -- see logs\"}";
    }
  }
}
