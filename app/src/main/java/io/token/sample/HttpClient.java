package io.token.sample;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final Gson gson = new Gson();

    /**
     * Sends HTTP Get Request
     *
     * @param url the url
     * @return the response as a json element
     */
    public static JsonObject sendGet(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            logger.info("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder res = new StringBuilder();

            while ((line = in.readLine()) != null) {
                res.append(line);
            }
            in.close();

            logger.info(res.toString());
            return new JsonParser().parse(res.toString()).getAsJsonObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Sends HTTP Post Request
     *
     * @param url the url
     * @param jsonParams the parameters in Json format
     * @return the response as a json element
     */
    public static JsonObject sendPost(URL url, Map<String, String> params) {
        return unsafeRequest(Method.POST, url, gson.toJson(params));
    }

    /**
     * Sends HTTP Put Request
     *
     * @param url the url
     * @param jsonParams the parameters in Json format
     * @return the response as a json element
     */
    public static JsonObject sendPut(URL url, Map<String, String> params) {
        return unsafeRequest(Method.PUT, url, gson.toJson(params));
    }

    private enum Method {
        POST, PUT
    }

    private static JsonObject unsafeRequest(Method method, URL url, String jsonParams) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod(method.name());
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(jsonParams);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            logger.info("Post parameters : " + jsonParams);
            logger.info("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            logger.info(response.toString());
            return new JsonParser().parse(response.toString()).getAsJsonObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
