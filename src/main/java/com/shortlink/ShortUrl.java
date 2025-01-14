package com.shortlink;

import java.time.LocalDateTime;

public class ShortUrl {
    private final String shortUrl;
    private final String longUrl;
    private final LocalDateTime createdAt;
    private final int clickLimit;
    private final int daysToLive;
    private int clicks;

    public ShortUrl(String shortUrl, String longUrl, int clickLimit, int daysToLive) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.createdAt = LocalDateTime.now();
        this.clickLimit = clickLimit;
        this.clicks = clickLimit;
        this.daysToLive = daysToLive;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public int getClicks() {
        return clicks;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public void decrementClicks() {
        clicks--;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(createdAt.plusDays(daysToLive));
    }

    public int getDaysToLive() {
        return daysToLive;
    }

    public String getLongUrl() {
        return longUrl;
    }
}
