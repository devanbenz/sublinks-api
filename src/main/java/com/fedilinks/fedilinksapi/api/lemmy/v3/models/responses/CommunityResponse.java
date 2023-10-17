package com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses;

import com.fedilinks.fedilinksapi.api.lemmy.v3.models.views.CommunityView;
import lombok.Builder;

import java.util.List;

@Builder
public record CommunityResponse(
        CommunityView community_view,
        List<String> discussion_languages
) {
}