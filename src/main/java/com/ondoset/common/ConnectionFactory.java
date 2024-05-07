package com.ondoset.common;

import org.apache.commons.dbcp2.*;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {

	private static BasicDataSource dataSource;

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			dataSource = new BasicDataSource();
			dataSource.setUrl("jdbc:mysql://localhost:3306/db24119?serverTimezone=Asia/Seoul");
			dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			dataSource.setUsername("root");
			dataSource.setPassword("root");
		}
		return dataSource.getConnection();
	}
}
