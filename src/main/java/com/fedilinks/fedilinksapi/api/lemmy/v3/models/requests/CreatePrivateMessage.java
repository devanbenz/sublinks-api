package com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests;

import lombok.Builder;

@Builder
public record CreatePrivateMessage(
        String content,
        int recipient_id,
        String auth
) {
}