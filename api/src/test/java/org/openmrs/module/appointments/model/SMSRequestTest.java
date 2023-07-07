package org.openmrs.module.appointments.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SMSRequestTest {

    @Test
    public void shouldSerializeToJSON() throws IOException {
        SMSRequest smsRequest = new SMSRequest();
        smsRequest.setPhoneNumber("1234567890");
        smsRequest.setMessage("Hello, World!");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(smsRequest);

        Assert.assertEquals("{\"phoneNumber\":\"1234567890\",\"message\":\"Hello, World!\"}", json);
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        String json = "{\"phoneNumber\":\"1234567890\",\"message\":\"Hello, World!\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        SMSRequest smsRequest = objectMapper.readValue(json, SMSRequest.class);

        Assert.assertEquals("1234567890", smsRequest.getPhoneNumber());
        Assert.assertEquals("Hello, World!", smsRequest.getMessage());
    }
}
