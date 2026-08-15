package com.magyen.platform.administration.presentation.user.response;

import java.util.List;

public record GetAuthenticationUsersResponse(
        List<AuthenticationUserResponse> users
) {
}
