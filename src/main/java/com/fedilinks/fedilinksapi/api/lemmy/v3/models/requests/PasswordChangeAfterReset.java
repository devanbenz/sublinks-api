package com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests;

import lombok.Builder;

@Builder
public record PasswordChangeAfterReset(
        String token,
        String password,
        String password_verify
) {
}