package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UrlDao {
	private String dbPath;

	public UrlDao(String dbPath) {
		this.dbPath = dbPath;
	}

	private Connection connect(String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

	// obtain the full URL given a shortened URL
	public String find(String shortURL) {
		String longURL = null;
		Connection conn = null;
		try {
			conn = connect(this.dbPath);
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
	public void save(String shortURL, String longURL) {
		Connection conn = null;
		try {
			conn = connect(this.dbPath);
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
}