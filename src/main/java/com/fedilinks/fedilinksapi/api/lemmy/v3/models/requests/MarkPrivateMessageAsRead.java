package com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests;

import lombok.Builder;

@Builder
public record MarkPrivateMessageAsRead(
        int private_message_id,
        boolean read,
        String auth
) {
}