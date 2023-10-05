package proxy;

import java.util.HashMap;
import java.util.*;

public class Cache {
  private Map<String, String> internal;

  public Cache() {
    this.internal = new HashMap();
  }

  String get(String key) {
    String value = this.internal.get(key);
    //System.out.println(String.format("CACHE: %s -> %s", key, value));
    return value;
  }

  void set(String key, String value) {
    this.internal.put(key, value);
    //System.out.println(String.format("CACHE: %s = %s", key, value));
  }

  public int size() {
    return this.internal.size();
  }
}