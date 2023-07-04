package org.openmrs.module.appointments.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.openmrs.module.appointments.properties.AppointmentProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppointmentVisitLocation {

    private static String openmrsBaseurl;

    public static String sendGetRequest(String url) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            reader.close();
            JSONObject json = new JSONObject(responseBody.toString());
            String name = json.optString("name");
            return name;
        } catch (org.json.JSONException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void setBaseURL(String baseURL) {
        if (AppointmentProperties.properties != null) {
            String baseUriProperty = AppointmentProperties.properties.getProperty("openmrs.root.uri");
            if (baseUriProperty != null && !baseUriProperty.isEmpty()) {
                this.openmrsBaseurl = baseUriProperty;
                return;
            }
        }
        this.openmrsBaseurl = baseURL;
    }

    public String getFacilityName(String locationUuid) {
        String baseUrl = openmrsBaseurl + "/bahmnicore/facilityLocation/";
        String visitLocationName = sendGetRequest(baseUrl + locationUuid);
        return visitLocationName;
    }
}
