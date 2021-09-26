package com.example.developerApp;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/")
public class SampleQueryJpmcController {

    @Value("${jpmc.basicAuth}")
    private String basicAuth;

    @RequestMapping(value = { "/test" })
    public ResponseEntity testJpmc() {
        // Let's obtain a token from Okta
        // Query the Okta IDP with this JWT
        WebClient client = WebClient.create();
        String remoteApiUrl = "https://id.unicorn-finance-dev.com/oauth2/auszuppqsU0dhKv5B1d6/v1/token";
        remoteApiUrl += "?grant_type=client_credentials";
        remoteApiUrl += "&scope=developerApp"; // Request the appropriate scope here
        String oktaResponse = client.post()
                .uri(remoteApiUrl)
                .header("cache-control", "no-cache")
                .header(HttpHeaders.AUTHORIZATION, "Basic "+basicAuth) // Basic here
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();

        JSONObject oktaResponseJSON = new JSONObject(oktaResponse);
        System.out.println("Okta response: " + oktaResponseJSON.toString());

        // Extract the access token with which Okta responded
        String accessToken = oktaResponseJSON.getString("access_token");
        System.out.println("access_token: " + accessToken);

        // Now let's send this token to the OAuth2 scope-protected JPMC API
        remoteApiUrl = "https://evmv7npsa2.execute-api.eu-west-1.amazonaws.com/api/devapp";

        String jpmcResponse = client.get()
                .uri(remoteApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken) // Bearer here
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();

        System.out.println("JPMC response: " + jpmcResponse);

        return ResponseEntity.ok("JPMC response: "+jpmcResponse+"\n");
    }

}
