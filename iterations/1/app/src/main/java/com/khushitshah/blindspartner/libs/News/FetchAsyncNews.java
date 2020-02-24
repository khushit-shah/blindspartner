package com.khushitshah.blindspartner.libs.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchAsyncNews {

    private static final String API_KEY = "a77b9f63b67642738c7679e9ecc646d0";
    private static final String HEADLINE_URL = "https://newsapi.org/v2/top-headlines?";
    private static final String EVERYTHING_URL = "https://newsapi.org/v2/everything?";


    /**
     * Static function thats fetchs news Asynchronously from newsapi.org! Credit added At the last of the news!
     *
     * @param country  Country for which to search news!
     * @param search   Search Query for the news.
     * @param topic    ** NOT IMPLEMENTED **
     * @param type     Either News is Everything or headlines
     * @param callback Callback!
     */
    public static void fetchNews(String country, String search, String topic, NewsTypes type, NewsFetchedResult callback) {
        Thread t = new Thread(() -> {
            String url = "";
            if (type == NewsTypes.EVERYTHING) {
                url = EVERYTHING_URL;
            } else {
                url = HEADLINE_URL;
            }
            url += "apiKey=" + API_KEY + "&";
            url += "country=" + country;
//            if (topic.length() >= 4) {
//                url += "&category" + topic;
//            }
            if (search.length() >= 3) {
                url += "&q" + search;
            }
            System.out.println("url : " + url);
            String res = getUrlResult(url);
            if (res.length() <= 2) {
                callback.newsFetched("can't fetch news try again later!");
            } else {
                try {
                    JSONObject object = new JSONObject(res);
                    if (!object.optString("status").equals("ok")) {
                        callback.newsFetched("can't fetch news try again later!");
                        return;
                    }
                    JSONArray articles = object.getJSONArray("articles");
                    for (int i = 0; i < articles.length() && i < 10; i++) {
                        JSONObject obj = (JSONObject) articles.get(i);
                        callback.newsFetched(obj.getString("title"));
                    }
                    callback.newsFetched("News Credits to newsapi.org ");
                } catch (JSONException e) {
                    callback.newsFetched("can't fetch news try again later!");
                }
            }
        });
        t.start();
    }


    private static String getUrlResult(String url) {
        try {
            URL fetchUrl = new URL(url);
            HttpURLConnection c = (HttpURLConnection) fetchUrl.openConnection();
            c.connect();
            return inputStreamToString(c.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    private static String inputStreamToString(InputStream inputStream) {
        try {
            StringBuilder res = new StringBuilder();
            int c;
            while ((c = inputStream.read()) != -1) {
                res.append((char) c);
            }
            return res.toString();
        } catch (Exception e) {
            // Problem with Stream reading.
            return "";
        }
    }
}
