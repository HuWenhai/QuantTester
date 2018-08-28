package helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import global.Config;

public final class MySQLHelper {
	public static final Connection getConnection(String schemaName) {
		Connection conn = null;

        try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + Config.hostName + ":" + Config.port + "/" + schemaName, Config.userName, Config.password);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.print("getConnection: MYSQL ERROR:" + e.getMessage());
		}

        return conn;
	}
}
