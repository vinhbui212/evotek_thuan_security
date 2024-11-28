package org.example.thuan_security.repository;


import feign.QueryMap;
import org.example.thuan_security.request.LogoutRequest;
import org.example.thuan_security.request.TokenExchangeParam;
import org.example.thuan_security.request.UserCreationParam;
import org.example.thuan_security.response.TokenExchangeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-client", url = "${idp.url}")
public interface IdentityClient {
    @PostMapping(value = "/realms/vinhbui21/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse exchangeToken(@QueryMap TokenExchangeParam param);

    @PostMapping(value = "/admin/realms/vinhbui21/users",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(
            @RequestHeader("authorization") String token,
            @RequestBody UserCreationParam param);

    @PostMapping(value = "/realms/vinhbui21/protocol/openid-connect/logout",
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String logout(@RequestHeader("authorization") String token, @QueryMap LogoutRequest request);
}

