package com.clickbait.tflow.exchange;

import java.util.List;

public class LinkScoreBatchRequest {
    private final List<LinkScoreRequest> links;

    public LinkScoreBatchRequest(List<LinkScoreRequest> links) {
        this.links = links;
    }

    public List<LinkScoreRequest> getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "{\"links\":" + getLinks() + "}";
    }
}
