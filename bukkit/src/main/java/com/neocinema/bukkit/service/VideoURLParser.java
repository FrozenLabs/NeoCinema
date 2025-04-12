package com.neocinema.bukkit.service;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.service.infofetcher.*;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoURLParser {

    private final NeoCinemaPlugin neoCinemaPlugin;
    private final String url;
    private boolean parsed;
    private VideoInfoFetcher infoFetcher;
    private String type = "unsupported";

    public VideoURLParser(NeoCinemaPlugin neoCinemaPlugin, String url) {
        this.neoCinemaPlugin = neoCinemaPlugin;
        this.url = url;
    }

    public void parse(Player player) {
        if (parsed) return;
        parsed = true;

        String youtubeVideoId = getLink(url, "https?://(?:m\\.)?(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?(?:feature=youtu.be&)?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)", 1);
        if (youtubeVideoId != null) {
            infoFetcher = new YouTubeVideoInfoFetcher(neoCinemaPlugin, youtubeVideoId);
            type = "youtube";
            return;
        }

        String twitchUser = getTwitchUser(url);
        if (twitchUser != null) {
            infoFetcher = new TwitchVideoInfoFetcher(twitchUser);
            type = "twitch";
            return;
        }

        String hlsLink = getLink(url, "^https?://[\\S\\-]+(\\.[\\S\\-]+)*/[\\S\\-]+\\.m3u8(\\?.*)?$", 0);
        if (hlsLink != null) {
            infoFetcher = new HLSVideoInfoFetcher(url, player == null ? "server" : player.getName());
            type = "hls";
            return;
        }

        String mediaLink = getLink(url, "^https?://[\\W\\w\\-]+(\\.[\\W\\w\\-]+)*/[\\W\\w\\-]+\\.(mp[34]|ts|ogg|opus|webm|m4v)(\\?.*)?$", 0);
        if (mediaLink != null) {
            infoFetcher = new FileVideoInfoFetcher("neocinema.request.file", url, player == null ? "server" : player.getName());
            type = "file";
        }
    }

    private static String getLink(String url, String regex, int group) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }

    private static String getTwitchUser(String url) {
        String[] parts = url.split("https://www.twitch.tv/");
        if (parts.length > 1) {
            String usernameCandidate = parts[1];
            if (!usernameCandidate.contains("/")) {
                return usernameCandidate.trim();
            }
        }
        return null;
    }

    public VideoInfoFetcher getInfoFetcher() {
        return infoFetcher;
    }

    public boolean found() {
        return infoFetcher != null;
    }

    public String getType() {
        return type;
    }
}
