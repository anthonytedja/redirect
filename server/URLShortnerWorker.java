package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class URLShortnerWorker implements Runnable {
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "../index.html";
	static final String FILE_NOT_FOUND = "../404.html";
	static final String METHOD_NOT_SUPPORTED = "../not_supported.html";
	static final String REDIRECT_RECORDED = "../redirect_recorded.html";
	static final String REDIRECT = "../redirect.html";
	static final String NOT_FOUND = "../notfound.html";

	// verbose mode
	private boolean VERBOSE;

	private int threadId;
	private ThreadWork work;
	private boolean isCacheEnabled;
	private boolean isWriteBufferEnabled;

	// specify 0 for no cache/buffer
	public URLShortnerWorker(int threadId, ThreadWork work, boolean verbose) {
		this.VERBOSE = verbose;

		this.threadId = threadId;
		this.work = work;
		this.isCacheEnabled = work.getCache().getMaxSize() <= 0 ? false : true; // disable if specified size is <= 0
		this.isWriteBufferEnabled = work.getWriteBuffer().getMaxSize() <= 0 ? false : true;
	}
	
	public void run() {
		// wait for socket to become available - check socket queue
		while (true) {
			// synchronization mechanisms force threads to wait until a socket becomes available
			try {
				Socket newClient = work.getQueue().dequeue();
				handle(newClient);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}
	
	public void handle(Socket connect) {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			String input = in.readLine();

			if (VERBOSE) {
				System.out.println(new Date() + ": Thread " + this.threadId + ": " + input);
			}
			Pattern pput = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
			Matcher mput = pput.matcher(input);
			if (mput.matches()) { // persist URL
				String shortResource = mput.group(1);
				String longResource = mput.group(2);
				// String httpVersion = mput.group(3);

				if (isWriteBufferEnabled) {
					this.work.getWriteBuffer().put(shortResource, longResource); // buffer automatically flushes
				} else {
					this.work.getUrlDao().save(shortResource, longResource);
				}

				if (isCacheEnabled) {
					this.work.getCache().put(shortResource, longResource);
				}

				File file = new File(WEB_ROOT, REDIRECT_RECORDED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				// read content to return to client
				byte[] fileData = readFileData(file, fileLength);

				out.println("HTTP/1.1 200 OK");
				out.println("Server: Java HTTP Server/Shortner : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println();
				out.flush();

				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
			} else { // retrieve URL
				Pattern pget = Pattern.compile("^(\\S+)\\s+/(\\S+)\\s+(\\S+)$");
				Matcher mget = pget.matcher(input);
				if (mget.matches()) {
					// String method = mget.group(1);
					String shortResource = mget.group(2);
					// String httpVersion = mget.group(3);

					String longResource = null;
					if (isCacheEnabled) {
						longResource = this.work.getCache().get(shortResource); // check in cache first
						if (longResource == null) {
							longResource = this.work.getUrlDao().find(shortResource);
							this.work.getCache().put(shortResource, longResource);
						}
					} else {
						longResource = this.work.getUrlDao().find(shortResource);
					}

					if (longResource != null) { // case 1: URL exists - display success page
						File file = new File(WEB_ROOT, REDIRECT);
						int fileLength = (int) file.length();
						String contentMimeType = "text/html";

						// read content to return to client
						byte[] fileData = readFileData(file, fileLength);

						// out.println("HTTP/1.1 301 Moved Permanently");
						out.println("HTTP/1.1 307 Temporary Redirect");
						out.println("Location: " + longResource);
						out.println("Server: Java HTTP Server/Shortner : 1.0");
						out.println("Date: " + new Date());
						out.println("Content-type: " + contentMimeType);
						out.println("Content-length: " + fileLength);
						out.println();
						out.flush();

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
					} else { // case 2: URL doesn't exist
						File file = new File(WEB_ROOT, FILE_NOT_FOUND);
						int fileLength = (int) file.length();
						String content = "text/html";
						byte[] fileData = readFileData(file, fileLength);

						out.println("HTTP/1.1 404 File Not Found");
						out.println("Server: Java HTTP Server/Shortner : 1.0");
						out.println("Date: " + new Date());
						out.println("Content-type: " + content);
						out.println("Content-length: " + fileLength);
						out.println();
						out.flush();

						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
				in.close();
				out.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if (VERBOSE) {
				System.out.println(new Date() + ": Thread " + this.threadId + ": Connection closed");
			}
		}
	}

	// load a file into memory
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}
}