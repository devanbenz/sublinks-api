package com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests;

import lombok.Builder;

@Builder
public record RemoveComment(
        int comment_id,
        boolean removed,
        String reason,
        String auth
) {
}