package com.magyen.platform.administration.application.dto;

import java.util.List;

public record ListAuthenticationUsersResult(
        List<AuthenticationUserView> users
) {
}
