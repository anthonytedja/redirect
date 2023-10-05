package proxy;

import java.io.*;
import java.net.*;

public class SimpleProxyServer {
  public static Cache readcache = new Cache();
  
  public static void main(String[] args) throws IOException {
    try {
      int hostport = Integer.parseInt(args[0]);
      System.out.println("Starting proxy on port " + hostport);
      runServer(hostport); // never returns
    } catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java SimpleProxyServer PORT");
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
      new SimpleProxyThread(ss.accept(), SimpleProxyServer.readcache).start();
      //new SimpleProxyThread(ss.accept()).start();

      System.out.print("Number of active threads: " + Thread.activeCount());
      System.out.println("\tCache size: " + readcache.size());
    }
  }
}
