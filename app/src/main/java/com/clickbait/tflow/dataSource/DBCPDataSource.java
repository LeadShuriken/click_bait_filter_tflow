package com.clickbait.tflow.dataSource;

import java.sql.Connection;
import java.sql.SQLException;

import com.clickbait.tflow.config.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class DBCPDataSource {

    private BasicDataSource ds = new BasicDataSource();

    private static DBCPDataSource sigleInstance = null;

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static DBCPDataSource getInstance(DataSource properties) {
        if (sigleInstance == null) {
            sigleInstance = new DBCPDataSource(properties);
        }
        return sigleInstance;
    }

    private DBCPDataSource(DataSource properties) {
        ds.setDriverClassName(properties.getDriver());
        ds.setUrl(properties.getUrl());
        ds.setUsername(properties.getUser());
        ds.setPassword(properties.getPassword());
        ds.setInitialSize(properties.getMinIdle());
        ds.setMaxActive(properties.getMaxActive());
    }
}