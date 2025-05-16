package com.team.child_be.dtos.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ExchangeTokenRequest(
    String code,
    String clientId,
    String clientSecret,
    String redirectUri,
    String grantType
) {}
