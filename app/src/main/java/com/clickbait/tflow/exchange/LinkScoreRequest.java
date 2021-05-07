package com.clickbait.tflow.exchange;

public class LinkScoreRequest {
    private final String name;
    private final float score;

    public LinkScoreRequest(String name, float score) {
        this.score = score;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + getName() + "\",\"score\":\"" + getScore() + "\"}";
    }
}
