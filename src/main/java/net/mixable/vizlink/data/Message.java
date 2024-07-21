package net.mixable.vizlink.data;

import java.time.Instant;

// see also reflect-config.json
public class Message {

  public Object payload;
  public Long ms;
  public String type;
  public Integer version;

  public Message() {}

  public Message(Object payload, Long ms, String type) {
    this.payload = payload;
    this.ms = ms;
    this.type = type;
    this.version = 1;
  }

  public Message(Object payload, String type) {
    this.payload = payload;
    this.ms = Instant.now().toEpochMilli();
    this.type = type;
    this.version = 1;
  }
}
