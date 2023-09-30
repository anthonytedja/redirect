import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLShortner {

	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	static final String REDIRECT_RECORDED = "redirect_recorded.html";
	static final String REDIRECT = "redirect.html";
	static final String NOT_FOUND = "notfound.html";

	// verbose mode
	static final boolean verbose = true;

	private static Connection connect(String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

	public static void main(String[] args) {
		try {
			// port to listen connection
			int PORT = Integer.parseInt(args[0]);
			// database url
			String DBPath = args[1];

			try (ServerSocket serverConnect = new ServerSocket(PORT)) {
				System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

				// we listen until user halts server execution
				while (true) {
					if (verbose) {
						System.out.println("Connection opened. (" + new Date() + ")");
					}
					handle(serverConnect.accept(), DBPath);
				}
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java [SQLITE JAR CLASSPATH] URLShortner.java [PORT] [JDBC DB URL]");
		}
	}

	public static void handle(Socket connect, String DBPath) {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			String input = in.readLine();

			if (verbose)
				System.out.println("first line: " + input);
			Pattern pput = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
			Matcher mput = pput.matcher(input);
			System.out.println(input);
			if (mput.matches()) { // persist URL
				String shortResource = mput.group(1);
				String longResource = mput.group(2);
				// String httpVersion = mput.group(3);

				save(shortResource, longResource, DBPath);

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

					String longResource = find(shortResource, DBPath);
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
			System.err.println("Server error");
		} finally {
			try {
				in.close();
				out.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
	}

	// obtain the full URL given a shortened URL
	private static String find(String shortURL, String DBPath) {
		String longURL = null;
		Connection conn = null;
		try {
			conn = connect(DBPath);
			String sql = "SELECT * FROM urls WHERE short_code = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, shortURL);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				longURL = rs.getString("url_original");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
		return longURL;
	}

	// persist the short and long URLs
	private static void save(String shortURL, String longURL, String DBPath) {
		Connection conn = null;
		try {
			conn = connect(DBPath);
			/**
			 * pragma locking_mode=EXCLUSIVE;
			 * pragma mmap_size = 30000000000;
			 * pragma temp_store = memory;
			 **/
			String sql = """
					 	pragma journal_mode = WAL;
						pragma synchronous = normal;
					""";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);

			String updateSQL = "INSERT OR REPLACE INTO urls (short_code, url_original) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(updateSQL);
			ps.setString(1, shortURL);
			ps.setString(2, longURL);
			ps.execute();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	// load a file into memory
	private static byte[] readFileData(File file, int fileLength) throws IOException {
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
