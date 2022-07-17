package sber.data.airportsClient;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Log4j2
public class AirportsClient {

    private static final String AIRPORTS_SERVER_URL = "http://localhost:1580/airports/airport";
    private static final String REQUEST = "REQUEST ";

    private final CloseableHttpClient closeableHttpClient;

    private final SimpleDateFormat simpleDateFormat;


    public void doPost(Integer id) {

        HttpPost httpPost = new HttpPost(AIRPORTS_SERVER_URL);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject body = buildRequestJson(id);

        log.debug("Request body JSON: " + body.toString());

        httpPost.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));

        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(httpPost);
        } catch (Exception e) {
            log.error("Exception during sending request", e);
        }

        if (response != null) {

            HttpEntity httpEntity = response.getEntity();
            try {

                JSONObject jsonObject = getResponseJson(httpEntity);

                JSONArray airportInfo = jsonObject.getJSONArray("airportInfo");
                List<Object> airportInfoList = airportInfo.toList();

                int idFromResponse = airportInfoList.isEmpty() ? -1 : NumberUtils.toInt((String) airportInfoList.get(0));

                if (id == idFromResponse) {
                    log.info("Success! Id and IdFromResponse match!");
                    log.info("Response body JSON: " + jsonObject.toString());
                } else {
                    log.warn("Failure! Id and IdFromResponse don't match!");
                }

            } catch (IOException | ClassCastException | NumberFormatException e) {
                log.error("Error during response parsing");
            }
        }

    }


    private JSONObject getResponseJson(HttpEntity httpEntity) throws IOException {
        Scanner scanner = new Scanner(httpEntity.getContent());
        String responseJson = scanner.nextLine();
        scanner.close();
        return new JSONObject(responseJson);
    }

    private JSONObject buildRequestJson(Integer id) {
        Date timestamp = new Date();

        JSONObject body = new JSONObject();
        body.put("id", id);
        synchronized (this) {
            body.put("timestamp", simpleDateFormat.format(timestamp));
        }
        body.put("uuid", generateUuid(timestamp));
        body.put("currentThreadName", Thread.currentThread().getName());

        return body;
    }

    private UUID generateUuid(Date timestamp) {
        String stringForUuid = REQUEST
                .concat(Thread.currentThread().getName())
                .concat(" ")
                .concat(String.valueOf(timestamp.getTime()));
        log.trace("String for UUID: \"{}\"", stringForUuid);

        UUID generatedUuid = UUID.nameUUIDFromBytes(stringForUuid.getBytes(StandardCharsets.UTF_8));
        log.trace("Generated UUID for request: \"{}\"", generatedUuid);

        return generatedUuid;
    }


    public AirportsClient() {
        this.closeableHttpClient = HttpClients.createDefault();
        this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
}
