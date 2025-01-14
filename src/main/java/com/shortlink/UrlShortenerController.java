package com.shortlink;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;


import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/url")
public class UrlShortenerController {
    private final Map<String, List<ShortUrl>> urlStore = new ConcurrentHashMap<>();
    private int defaultDaysToLive;

    @PostMapping("/short")
    public Map<String, Object> shortenUrl(@RequestParam(value = "UUID", required = false, defaultValue = "") String userUUID,
                               @RequestParam String longUrl,
                               @RequestParam(value = "clickLimit", required = false, defaultValue = "10") int clickLimit,
                               @RequestParam(value = "daysToLive", required = false) int daysToLive) {
        if (Objects.equals(userUUID, "")) {
            userUUID = UUID.randomUUID().toString();
        }

        if (Objects.equals(daysToLive, null)) {
            daysToLive = getDefaultDaysToLive();
        }

        String url = generateShortUrl(longUrl, userUUID);
        ShortUrl shortUrl = new ShortUrl(url, longUrl, clickLimit, daysToLive);
        urlStore.computeIfAbsent(userUUID, k -> new ArrayList<>()).add(shortUrl);

        return Map.of("UUID", userUUID, "shortURL", shortUrl.getShortUrl(),
                "clickLimit", shortUrl.getClickLimit(), "expired", shortUrl.isExpired());
    }

    @GetMapping("/short/{userUUID}")
    public ResponseEntity<Object> getAllShortUrlsForUser(@PathVariable String userUUID) {
        List<ShortUrl> shortUrls = urlStore.get(userUUID);

        if (shortUrls == null || shortUrls.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No URLs found for the given UUID"));
        }

        // Формируем ответ с данными о всех ссылках
        List<Map<String, Object>> response = shortUrls.stream()
                .map(shortUrl -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("shortUrl", shortUrl.getShortUrl());
                    map.put("longUrl", shortUrl.getLongUrl());
                    map.put("clickLimit", shortUrl.getClickLimit());
                    map.put("daysToLive", shortUrl.getDaysToLive());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/redirect")
    public ResponseEntity<String> redirectToLongUrl(@RequestParam String userUUID,
                                                    @RequestParam String shortUrl){

        if (userUUID == null || userUUID.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing UUID.");
        }

        List<ShortUrl> shortUrls = urlStore.get(userUUID);

        if (shortUrls == null || shortUrls.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URLs don't exist."); // Пользователь не найден
        }

        ShortUrl targetUrl = shortUrls.stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst()
                .orElse(null);

        if (targetUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Ссылка не найдена
        }

        if (targetUrl.getClicks() > 0 && !targetUrl.isExpired()) {
            targetUrl.decrementClicks();
            return ResponseEntity.status(302)
                    .location(URI.create(targetUrl.getLongUrl()))
                    .build();
        } else {
            if (targetUrl.getClicks() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL is expired.");
            }
            else {
                shortUrls.remove(targetUrl);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL doesn't exist.");
            }
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateClickLimit(@RequestParam String userUUID,
                                                    @RequestParam String shortUrl,
                                                   @RequestParam int clickLimit){

        if (userUUID == null || userUUID.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing UUID.");
        }

        List<ShortUrl> shortUrls = urlStore.get(userUUID);

        if (shortUrls == null || shortUrls.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URLs don't exist."); // Пользователь не найден
        }

        ShortUrl targetUrl = shortUrls.stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst()
                .orElse(null);

        if (targetUrl == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL NOT FOUND."); // Ссылка не найдена
        }

        targetUrl.setClickLimit(clickLimit);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("URL clickLimit is updated");
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteURL(@RequestParam String userUUID,
                                                   @RequestParam String shortUrl){

        if (userUUID == null || userUUID.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing UUID.");
        }

        List<ShortUrl> shortUrls = urlStore.get(userUUID);

        if (shortUrls == null || shortUrls.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URLs don't exist."); // Пользователь не найден
        }

        ShortUrl targetUrl = shortUrls.stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst()
                .orElse(null);

        if (targetUrl == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL NOT FOUND."); // Ссылка не найдена
        }

        shortUrls.remove(targetUrl);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("URL is deleted");
    }

    @ConfigurationProperties(prefix = "url.shortener")
    public int getDefaultDaysToLive() {
        return defaultDaysToLive;
    }

    private String generateShortUrl(String longUrl, String userUUID) {
        int hashCode = (longUrl + userUUID + LocalDateTime.now().toString()).hashCode();

        String base64Hash = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(Integer.toString(hashCode).getBytes());

        return "clck.ru/" + base64Hash.substring(0, 6);
    }
}
