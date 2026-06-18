package br.com.budgeting.ia;

import java.util.UUID;

public class LatestInteraction {
    private String id;
    private String type;
    private String query;
    private String response;
    private long timestamp;

    public LatestInteraction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public LatestInteraction(String type, String query, String response) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.query = query;
        this.response = response;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
