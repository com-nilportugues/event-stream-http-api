package com.nilportugues.eventstore.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @RequestMapping("/")
    public StreamingResponseBody welcome() throws Exception {
        return out -> {
            URL url = new URL("http://localhost:8090/infinite-json");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                reader.lines().forEach(line -> {
                    try {
                        out.write((line + "\n").getBytes());
                        out.flush();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                        return;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

        };
    }


    @RequestMapping(method = RequestMethod.GET, value = "/infinite-json", produces = "text/event-stream")
    public StreamingResponseBody handleRequest() {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        return out -> {
            for (int i = 0; i < 100; i++) {
                String nowAsISO = String.valueOf(new Date().toInstant().getEpochSecond());

                out.write(("{\"id\": " + i + ", \"hello\": \"" + nowAsISO + "\"}").getBytes());
                out.write(("\n").getBytes());
                out.flush();

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
