package com.clickbait.tflow.exchange;

public class LinkScoreRequest {
    private final String link;

    public LinkScoreRequest(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "LinkScoreRequest{link=" + getLink() + "}";
    }
}
