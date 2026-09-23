package Model;

import java.util.Map;

/**
 * AudioBook Model - Demonstrates OOP Inheritance & Interface implementation.
 * Satisfies Day 1 requirement for a new book type.
 */
public class AudioBook extends Book implements DigitalAccessible {
    private int audioDurationMinutes;
    private String narrator;
    private String audioStreamUrl;

    public AudioBook(String bookId, String title, String author, String genre,
                     int audioDurationMinutes, String narrator, String audioStreamUrl,
                     double buyPrice) {
        super(bookId, title, author, genre, buyPrice);
        this.audioDurationMinutes = audioDurationMinutes;
        this.narrator = narrator;
        this.audioStreamUrl = audioStreamUrl;
    }

    public AudioBook(String bookId, String title, String author, String genre,
                     int audioDurationMinutes, String narrator, String audioStreamUrl,
                     double buyPrice, boolean available) {
        super(bookId, title, author, genre, buyPrice, available);
        this.audioDurationMinutes = audioDurationMinutes;
        this.narrator = narrator;
        this.audioStreamUrl = audioStreamUrl;
    }

    public int getAudioDurationMinutes() { return audioDurationMinutes; }
    public void setAudioDurationMinutes(int minutes) { this.audioDurationMinutes = minutes; }

    public String getNarrator() { return narrator; }
    public void setNarrator(String narrator) { this.narrator = narrator; }

    public String getAudioStreamUrl() { return audioStreamUrl; }
    public void setAudioStreamUrl(String url) { this.audioStreamUrl = url; }

    // Polymorphic Rental Pricing for AudioBooks (₹15/day)
    @Override
    public double getRentalPrice(int days) {
        return days * 15.0;
    }

    @Override
    public String getBookType() {
        return "AUDIOBOOK";
    }

    @Override
    public void download() {
        System.out.println("Streaming AudioBook: " + getTitle() + " narrated by " + narrator);
    }

    @Override
    public String getAccessUrl() {
        return audioStreamUrl;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("audioDurationMinutes", audioDurationMinutes);
        map.put("narrator", narrator);
        map.put("audioStreamUrl", audioStreamUrl);
        return map;
    }
}
