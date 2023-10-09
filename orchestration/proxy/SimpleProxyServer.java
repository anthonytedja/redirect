package proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;

public class SimpleProxyServer {
	// configurable parameters
	static boolean IS_VERBOSE; // toggle log statements
	static int CACHE_SIZE; // 1; NOT USED YET
	static int NUM_THREADS; // 4
	static int PROXY_PORT;
	static int HOST_PORT;

	public static void main(String[] args) {
		try {
			IS_VERBOSE = Boolean.parseBoolean(args[0]);
			PROXY_PORT = Integer.parseInt(args[1]);
			HOST_PORT = getHostPort();
			CACHE_SIZE = Integer.parseInt(args[2]);
			NUM_THREADS = Integer.parseInt(args[3]);

			if (IS_VERBOSE) {
				System.out.println("Starting proxy...");
				System.out.println("Cache size: " + CACHE_SIZE);
				System.out.println("Number of threads: " + NUM_THREADS);
			}

			runServer();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java SimpleProxyServer IS_VERBOSE PROXY_PORT CACHE_SIZE NUM_THREADS");
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static List<String> readLinesFromFile(String filepath) throws FileNotFoundException {
		List<String> lines = new ArrayList<String>();
		
		File file = new File(filepath);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine()) {
			lines.add(scanner.nextLine());
		}

		scanner.close();

		return lines;
	}

	private static int getHostPort() throws FileNotFoundException {
		List<String> lines = readLinesFromFile("HOSTPORT");
		return Integer.parseInt(lines.get(0));
	}

	private static List<String> getInitialHosts() throws FileNotFoundException {
		List<String> lines = readLinesFromFile("HOSTS");
		return lines;
	}

	/**
	 * runs a single-threaded proxy server on
	 * the specified local port. It never returns.
	 */
	private static void runServer() throws IOException, InterruptedException {
		ThreadWork work = new ThreadWork(CACHE_SIZE);

		List<String> hosts = getInitialHosts();
		for (String host : hosts) {
			work.getHostPool().addHost(host);
		}
		if (IS_VERBOSE) {
			System.out.println(work.getHostPool());
		}

		// start up worker threads to handle general URL shortening functionality
		Thread[] worker = new Thread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			worker[i] = new SimpleProxyThread(i, work, HOST_PORT, IS_VERBOSE);
			worker[i].start();
		}
		
		ServerSocket serverConnect = new ServerSocket(PROXY_PORT);
		System.out.println(new Date() + ": Proxy started.\nListening for connections on port : " + PROXY_PORT + " ...\n");

		// listen until user halts server execution
		while (true) {
			Socket socket = serverConnect.accept();
			work.getQueue().enqueue(socket);
			if (IS_VERBOSE) {
				System.out.println(new Date() + ": Connection opened");
			}
		}
	}
}
