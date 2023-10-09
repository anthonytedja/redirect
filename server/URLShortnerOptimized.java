package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class URLShortnerOptimized {

	// configurable parameters
	static boolean IS_VERBOSE; // toggle log statements
	static int PORT;
	static String DB_PATH;
	static int CACHE_SIZE; // 1; NOT USED YET
	static int WRITE_BUFFER_SIZE; // 1000; NOT USED YET
	static int NUM_THREADS; // 4
	static int SLEEP_DURATION; // 60000; 1 min

	public static void main(String[] args) {
		try {
			IS_VERBOSE = Boolean.parseBoolean(args[0]);
			PORT = Integer.parseInt(args[1]);
			DB_PATH = args[2];
			CACHE_SIZE = Integer.parseInt(args[3]);
			WRITE_BUFFER_SIZE = Integer.parseInt(args[4]);
			NUM_THREADS = Integer.parseInt(args[5]);
			SLEEP_DURATION = Integer.parseInt(args[6]);

			ThreadWork work = new ThreadWork(CACHE_SIZE, WRITE_BUFFER_SIZE, DB_PATH);

			// create persister thread to periodically flush write buffer into database
			if (WRITE_BUFFER_SIZE >= 0) {
				Thread writer = new Thread(new DatabaseWriteWorker(work, SLEEP_DURATION, IS_VERBOSE));
				writer.start();
			}

			// start up worker threads to handle general URL shortening functionality
			Thread[] worker = new Thread[NUM_THREADS];
			for (int i = 0; i < NUM_THREADS; i++) {
				worker[i] = new Thread(new URLShortnerWorker(i, work, IS_VERBOSE));
				worker[i].start();
			}
			
			try (ServerSocket serverConnect = new ServerSocket(PORT)) {
				System.out.println(new Date() + ": Server started.\nListening for connections on port : " + PORT + " ...\n");

				// listen until user halts server execution
				while (true) {
					Socket socket = serverConnect.accept();
					work.getQueue().enqueue(socket);
					if (IS_VERBOSE) {
						System.out.println(new Date() + ": Connection opened");
					}
				}
			}
		} catch (IOException e) {
			System.err.println(new Date() + ": Server Connection error : " + e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java [SQLITE JAR CLASSPATH] URLShortner.java [PORT] [JDBC DB URL]");
		} catch (Exception e) {
			System.err.println(new Date() + e.getMessage());
		}
	}
}
