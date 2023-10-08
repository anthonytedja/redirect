package proxy;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.util.Arrays;

public class SimpleProxyServer {
	public static Cache readcache = new Cache();

	// configurable parameters
	static boolean IS_VERBOSE; // toggle log statements
	static int PROXY_PORT;
	static String DB_PATH;
	static int CACHE_SIZE; // 1; NOT USED YET
	static int WRITE_BUFFER_SIZE; // 1000; NOT USED YET
	static int NUM_THREADS; // 4
	static int SLEEP_DURATION; // 60000; 1 min


	public static void main(String[] args) throws IOException {
		try {
			PROXY_PORT = Integer.parseInt(args[0]);
			System.out.println(new Date() + ": Starting proxy on port " + PROXY_PORT);
			runServer(PROXY_PORT); // never returns
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java SimpleProxyServer PORT");
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static List<String> getInitialHosts() { // read from HOSTS file
		return Arrays.asList(
			"dh2026pc21",
			"dh2026pc22",
			"dh2026pc23",
			"dh2026pc24");
	}

	/**
	 * runs a single-threaded proxy server on
	 * the specified local port. It never returns.
	 */
	public static void runServer(int localport) throws IOException {
		// Create a ServerSocket to listen for connections with
		ServerSocket ss = new ServerSocket(localport);

		List<String> hosts = getInitialHosts();
		ThreadWork work = new ThreadWork();
		for (String host : hosts) {
			work.getHostPool().addHost(host);
		}

		System.out.println(work.getHostPool());

		while (true) {
			new SimpleProxyThread(ss.accept(), work, SimpleProxyServer.readcache).start();

			System.out.print(new Date() + ": Number of active threads: " + Thread.activeCount());
			System.out.println("\tCache size: " + readcache.size());
		}
	}
}
