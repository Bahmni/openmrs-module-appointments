package org.openmrs.module.appointments.telehealth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Basic;

@Service
public class TeleHealthServiceImpl implements TeleHealthService {

    private String token;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Boolean authenticate() {
        String uri = "https://dev-medecin-hug-at-home.oniabsis.com/api/v1/login-local";
        User user = new User();
        user.setEmail("cicr@test.org");
        user.setPassword("123456");
        UserAuthResponse userAuthResponse = restTemplate.postForObject(uri, user, UserAuthResponse.class);
        token = userAuthResponse.getUser().getToken();
        return true;
    }

    @Override
    public Invite invite(InvitationRequest invitationRequest) {

        if (authenticate()) {
            String uri = "https://dev-medecin-hug-at-home.oniabsis.com/api/v1/invite/";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", token);
            HttpEntity<InvitationRequest> request = new HttpEntity<>(invitationRequest, headers);

            InvitationResponse response = null;
            if(invitationRequest.getId()==null){
                response = restTemplate.postForObject(uri, request, InvitationResponse.class);
                return response.getInvite();
            }else{
                uri += invitationRequest.getId();
                headers.set("X-HTTP-Method-Override", "PATCH");
                String s;

                //restTemplate.exchange(uri, HttpMethod.PATCH, request, String.class, 1);
                return restTemplate.postForObject(uri, request, Invite.class);
            }
        }
        return null;
    }

    @Override
    public void delete(String invitationID) {
        if (authenticate()) {
            String uri = "https://dev-medecin-hug-at-home.oniabsis.com/api/v1/invite/" + invitationID;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", token);
            HttpEntity<InvitationRequest> request = new HttpEntity<>(headers);
            restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, 1);
        }
    }

}
