package gofd.gFMenu.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Queries the latest published Trinity release; call from an asynchronous task. */
public final class UpdateChecker {
    private static final String RELEASE_API =
            "https://api.github.com/repos/Trinity-Plugins/GFMenu/releases/latest";
    private static final String RELEASE_PAGE = "https://github.com/Trinity-Plugins/GFMenu/releases";
    private static final int MAX_RESPONSE_CHARS = 1_000_000;
    private static final int MAX_CHANGELOG_CHARS = 1_000;

    private UpdateChecker() {
    }

    public static Result check(String currentVersion) {
        if (!VersionComparator.isValid(currentVersion)) {
            return Result.failure();
        }
        try {
            String json = request();
            String latestVersion = findJsonString(json, "tag_name");
            if (!VersionComparator.isValid(latestVersion)) {
                return Result.failure();
            }
            if (VersionComparator.compare(latestVersion, currentVersion) <= 0) {
                return Result.latest();
            }

            String releaseUrl = findJsonString(json, "html_url");
            if (releaseUrl == null || releaseUrl.isBlank()) {
                releaseUrl = RELEASE_PAGE;
            }
            String changelog = findJsonString(json, "body");
            if (changelog != null && changelog.length() > MAX_CHANGELOG_CHARS) {
                changelog = changelog.substring(0, MAX_CHANGELOG_CHARS) + "…";
            }
            return new Result(latestVersion, releaseUrl, changelog, true, false);
        } catch (IOException exception) {
            return Result.failure();
        }
    }

    private static String request() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(RELEASE_API).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "GFMenu-UpdateChecker");
        connection.setConnectTimeout(3_000);
        connection.setReadTimeout(5_000);
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Release service returned HTTP " + responseCode);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (response.length() + line.length() > MAX_RESPONSE_CHARS) {
                        throw new IOException("Release response exceeded size limit");
                    }
                    response.append(line).append('\n');
                }
                return response.toString();
            }
        } finally {
            connection.disconnect();
        }
    }

    static String findJsonString(String json, String key) {
        Pattern field = Pattern.compile("\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = field.matcher(json);
        return matcher.find() ? unescapeJsonString(matcher.group(1)) : null;
    }

    private static String unescapeJsonString(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character != '\\' || index + 1 >= value.length()) {
                decoded.append(character);
                continue;
            }
            char escape = value.charAt(++index);
            switch (escape) {
                case '"' -> decoded.append('"');
                case '\\' -> decoded.append('\\');
                case '/' -> decoded.append('/');
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'n' -> decoded.append('\n');
                case 'r' -> decoded.append('\r');
                case 't' -> decoded.append('\t');
                case 'u' -> {
                    if (index + 4 >= value.length()) {
                        return decoded.toString();
                    }
                    try {
                        decoded.append((char) Integer.parseInt(value.substring(index + 1, index + 5), 16));
                        index += 4;
                    } catch (NumberFormatException exception) {
                        decoded.append('u');
                    }
                }
                default -> decoded.append(escape);
            }
        }
        return decoded.toString();
    }

    public record Result(String version, String downloadUrl, String changelog, boolean updateAvailable,
                         boolean failed) {
        public static Result latest() {
            return new Result(null, null, null, false, false);
        }

        public static Result failure() {
            return new Result(null, null, null, false, true);
        }
    }
}
