package net.mixable.vizlink.data;

import org.deepsymmetry.beatlink.DeviceAnnouncement;

// see also reflect-config.json
public class Device {

  public Boolean active;
  public String name;
  public Integer player;

  public Device() {}

  public Device(DeviceAnnouncement a, Boolean active) {
    this.active = active;
    name = a.getDeviceName();
    player = a.getDeviceNumber();
  }
}
