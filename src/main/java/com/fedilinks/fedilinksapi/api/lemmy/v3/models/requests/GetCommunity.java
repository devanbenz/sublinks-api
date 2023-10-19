package com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests;

import lombok.Builder;

@Builder
public record GetCommunity(
        Long id,
        String name,
        String auth
) {
}