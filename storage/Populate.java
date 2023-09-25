package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class Populate {

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
		String url = args[0];
		Integer count = args.length > 1 ? Integer.parseInt(args[1]) : 0;
		System.out.println("Populating " + count + " records");
		write(url, count);
		// read(url);
	}

	public static void write(String url, Integer count) {
		Connection conn = null;
		try {
			conn = connect(url);
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

			String insertSQL = "INSERT INTO urls (short_code, url_original) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(insertSQL);
			for (int i = 0; i < count; i++) {
				ps.setString(1, "code" + i);
				ps.setString(2, "https://" + i);
				ps.execute();
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
	}

	public static void read(String url) {
		Connection conn = null;
		try {
			conn = connect(url);
			Statement stmt = conn.createStatement();
			String sql = "SELECT short_code, url_original FROM urls";
			ResultSet rs = stmt.executeQuery(sql);
			int count = 0;
			while (rs.next()) {
				count++;
				// System.out.println( rs.getString("short_code") + "\t" +
				// rs.getString("url_original")
				// );
			}
			System.out.println("Found " + count + " records");
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
}
