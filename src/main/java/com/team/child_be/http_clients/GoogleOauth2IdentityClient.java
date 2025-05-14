package com.team.child_be.http_clients;

import com.team.child_be.dtos.requests.ExchangeTokenRequest;
import com.team.child_be.dtos.responses.ExchangeTokenResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "oauth2-identity", url = "https://oauth2.googleapis.com")
public interface GoogleOauth2IdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest exchangeTokenRequest);
}
