package com.clickbait.tflow.config;

import java.util.Map;

public class ApplicationConfig {
    private Server server;
    private Encryption encryption;
    private DataSource datasource;
    private Map<String, Endpoint> endpoints;
    private NLPModel nlpModel;

    public Server getServer() {
        return server;
    }

    public NLPModel getNlpModel() {
        return nlpModel;
    }

    public void setNlpModel(NLPModel nlpModel) {
        this.nlpModel = nlpModel;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    public Map<String, Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, Endpoint> endpoints) {
        this.endpoints = endpoints;
    }
}
