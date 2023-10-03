package proxy;

import java.io.*;
import java.net.*;

public class SimpleProxyServer {
  //public Map<string, string> cache = new HashMap();
  
  public static void main(String[] args) throws IOException {
    try {
      int localport = 8000;
      System.out.println("Starting proxy on port " + localport);
      runServer(localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(int localport) throws IOException {
    // Create a ServerSocket to listen for connections with
    ServerSocket ss = new ServerSocket(localport);

    while (true) {
      new SimpleProxyThread(ss.accept()).start();
    }
  }
}
