package test.helpers;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlHelper {
    private URL url;

    public UrlHelper(String urlString) {
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        String[] segments = url.getPath().split("/");
        return segments[segments.length - 1];
    }
}
