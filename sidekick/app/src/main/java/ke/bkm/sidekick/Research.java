package ke.bkm.sidekick;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Looks up a question the user asked. The request body is only that question.
 * Message text, names, and notification text are never added.
 */
final class Research {
    private Research() {}

    static String answer(Context context, String question) {
        String key = SidekickPrefs.aiKey(context);
        if (key == null || key.length() < 10) return "";
        String asked = question == null ? "" : question.trim();
        if (asked.length() > 400) asked = asked.substring(0, 400);
        String text = call(key, "gemini-2.5-flash", asked);
        if (text == null) text = call(key, "gemini-2.0-flash", asked);
        if (text == null || text.isEmpty()) return "I could not reach research. Try again in a moment.";
        if (text.length() > 350) text = text.substring(0, 347) + ".";
        return text;
    }

    private static String call(String key, String model, String question) {
        HttpURLConnection connection = null;
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + Uri.encode(key);
            JSONObject part = new JSONObject();
            part.put("text", "You are BKM, a sidekick on a phone. Answer in one or two short spoken sentences. No markdown. " + question);
            JSONArray parts = new JSONArray().put(part);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONObject root = new JSONObject().put("contents", new JSONArray().put(content));
            root.put("generationConfig", new JSONObject().put("maxOutputTokens", 120));
            byte[] body = root.toString().getBytes(StandardCharsets.UTF_8);

            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(body);
            out.close();
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) return null;
            return parse(read(connection.getInputStream()));
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String parse(String json) {
        try {
            JSONObject response = new JSONObject(json);
            return response.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String read(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) builder.append(line);
        reader.close();
        return builder.toString();
    }
}
