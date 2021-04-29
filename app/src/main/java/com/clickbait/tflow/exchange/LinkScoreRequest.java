package com.clickbait.tflow.exchange;

public class LinkScoreRequest {
    private final String link;
    private final String score;

    public LinkScoreRequest(String link, String score) {
        this.link = link;
        this.score = score;
    }

    public String getLink() {
        return link;
    }

    public String getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "{\"link\":\"" + getLink() + "\",\"score\":\"" + getScore() + "\"}";
    }
}
