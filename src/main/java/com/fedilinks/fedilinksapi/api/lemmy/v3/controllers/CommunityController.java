package com.fedilinks.fedilinksapi.api.lemmy.v3.controllers;

import com.fedilinks.fedilinksapi.api.lemmy.v3.enums.SubscribedType;
import com.fedilinks.fedilinksapi.api.lemmy.v3.mappers.LemmyCommunityMapper;
import com.fedilinks.fedilinksapi.api.lemmy.v3.mappers.request.CreateCommunityFormMapper;
import com.fedilinks.fedilinksapi.api.lemmy.v3.mappers.response.CommunityResponseMapper;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.AddModToCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.BanPerson;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.BlockCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.CreateCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.DeleteCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.EditCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.FollowCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.GetCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.HideCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.ListCommunities;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.RemoveCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.requests.TransferCommunity;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.AddModToCommunityResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.BanFromCommunityResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.BlockCommunityResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.CommunityResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.GetCommunityResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.responses.ListCommunitiesResponse;
import com.fedilinks.fedilinksapi.api.lemmy.v3.models.views.CommunityView;
import com.fedilinks.fedilinksapi.authorization.AuthorizationService;
import com.fedilinks.fedilinksapi.authorization.enums.AuthorizeAction;
import com.fedilinks.fedilinksapi.authorization.enums.AuthorizedEntityType;
import com.fedilinks.fedilinksapi.community.Community;
import com.fedilinks.fedilinksapi.community.CommunityRepository;
import com.fedilinks.fedilinksapi.instance.LocalInstanceContext;
import com.fedilinks.fedilinksapi.language.Language;
import com.fedilinks.fedilinksapi.person.Person;
import com.fedilinks.fedilinksapi.util.KeyService;
import com.fedilinks.fedilinksapi.util.KeyStore;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v3/community")
public class CommunityController {
    private final LocalInstanceContext localInstanceContext;

    private final CommunityRepository communityRepository;

    private final CreateCommunityFormMapper createCommunityFormMapper;

    private final KeyService keyService;

    private final AuthorizationService authorizationService;

    private final CommunityResponseMapper communityResponseMapper;

    private final LemmyCommunityMapper lemmyCommunityMapper;

    public CommunityController(LocalInstanceContext localInstanceContext, CommunityRepository communityRepository, CreateCommunityFormMapper createCommunityFormMapper, KeyService keyService, AuthorizationService authorizationService, CommunityResponseMapper communityResponseMapper, LemmyCommunityMapper lemmyCommunityMapper) {
        this.localInstanceContext = localInstanceContext;
        this.communityRepository = communityRepository;
        this.createCommunityFormMapper = createCommunityFormMapper;
        this.keyService = keyService;
        this.authorizationService = authorizationService;
        this.communityResponseMapper = communityResponseMapper;
        this.lemmyCommunityMapper = lemmyCommunityMapper;
    }

    @PostMapping
    @Transactional
    public CommunityResponse create(@Valid @RequestBody CreateCommunity createCommunityForm, UsernamePasswordAuthenticationToken principal) {
        authorizationService
                .canPerson((Person) principal.getPrincipal())
                .performTheAction(AuthorizeAction.create)
                .onEntity(AuthorizedEntityType.community)
                .defaultingToAllow() // @todo use site setting to allow community creation
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        final List<Language> languages = new ArrayList<>();
        for (String languageCode : createCommunityForm.discussion_languages()) {
            final Optional<Language> language = localInstanceContext.languageRepository().findById(Long.valueOf(languageCode));
            language.ifPresent(languages::add);
        }
        KeyStore keys = keyService.generate();
        Community community = createCommunityFormMapper.createCommunityFormToCommunity(
                createCommunityForm,
                localInstanceContext.instance(),
                keys
        );
        community.setLanguages(languages);

        communityRepository.saveAndFlush(community);

        CommunityView communityView = lemmyCommunityMapper.communityToCommunityView(
                community,
                SubscribedType.Subscribed,
                false,
                community.getCommunityAggregates()
        );

        List<String> languageCodes = new ArrayList<>();
        for (Language langauge :
                languages) {
            languageCodes.add(langauge.getCode());
        }

        return communityResponseMapper.map(
                communityView,
                languageCodes
        );
    }

    @GetMapping
    GetCommunityResponse show(@Valid GetCommunity getCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping
    CommunityResponse update(@Valid EditCommunity editCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping("hide")
    CommunityResponse hide(@Valid HideCommunity hideCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("list")
    ListCommunitiesResponse list(@Valid ListCommunities listCommunitiesForm) {
        Collection<CommunityView> communities = new HashSet<>();
        return ListCommunitiesResponse.builder()
                .communities(communities)
                .build();
    }

    @PutMapping("follow")
    CommunityResponse follow(@Valid FollowCommunity followCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("block")
    BlockCommunityResponse block(@Valid BlockCommunity blockCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("delete")
    CommunityResponse delete(@Valid DeleteCommunity deleteCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("remove")
    CommunityResponse remove(@Valid RemoveCommunity removeCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("transfer")
    GetCommunityResponse transfer(@Valid TransferCommunity transferCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("ban_user")
    BanFromCommunityResponse banUser(@Valid BanPerson banPersonForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("mod")
    AddModToCommunityResponse addMod(@Valid AddModToCommunity addModToCommunityForm) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
}
