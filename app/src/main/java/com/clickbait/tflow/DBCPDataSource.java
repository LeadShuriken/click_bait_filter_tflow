package com.clickbait.tflow;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.dbcp.BasicDataSource;

public class DBCPDataSource {

    private BasicDataSource ds = new BasicDataSource();

    private static DBCPDataSource sigleInstance = null;

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static DBCPDataSource getInstance(Properties properties) {
        if (sigleInstance == null) {
            sigleInstance = new DBCPDataSource(properties);
        }
        return sigleInstance;
    }

    private DBCPDataSource(Properties properties) {
        ds.setDriverClassName(properties.getProperty("driver"));
        ds.setUrl(properties.getProperty("url"));
        ds.setUsername(properties.getProperty("user"));
        ds.setPassword(properties.getProperty("password"));
        ds.setInitialSize(Integer.parseInt(properties.getProperty("minIdle")));
        ds.setMaxActive(Integer.parseInt(properties.getProperty("maxActive")));
    }
}