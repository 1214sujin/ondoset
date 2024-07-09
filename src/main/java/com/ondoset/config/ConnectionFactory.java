package com.ondoset.config;

import org.apache.commons.dbcp2.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionFactory {

	private static BasicDataSource dataSource;
	private static String url;
	private static String driver;
	private static String username;
	private static String password;

	@Value("${spring.datasource.url}")
	public void setUrl(String url) {
		ConnectionFactory.url = url;
	}
	@Value("${spring.datasource.driver-class-name}")
	public void setDriver(String driver) {
		ConnectionFactory.driver = driver;
	}
	@Value("${spring.datasource.username}")
	public void setUsername(String username) {
		ConnectionFactory.username = username;
	}
	@Value("${spring.datasource.password}")
	public void setPassword(String password) {
		ConnectionFactory.password = password;
	}

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			dataSource = new BasicDataSource();
			dataSource.setUrl(url);
			dataSource.setDriverClassName(driver);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
		}
		return dataSource.getConnection();
	}
}
