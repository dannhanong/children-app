package com.team.child_be.http_clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.team.child_be.dtos.responses.GoogleOauth2UserResponse;

@FeignClient(name = "oauth2-user-client", url = "https://www.googleapis.com")
public interface GoogleOauth2UserClient {
    @GetMapping(value = "/oauth2/v1/userinfo")
    GoogleOauth2UserResponse getUserInfo(@RequestParam("alt") String alt,
                                   @RequestParam("access_token") String accessToken);
}
