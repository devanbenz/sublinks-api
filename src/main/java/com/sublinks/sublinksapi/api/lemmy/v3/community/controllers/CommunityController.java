package com.sublinks.sublinksapi.api.lemmy.v3.community.controllers;

import com.sublinks.sublinksapi.api.lemmy.v3.authentication.JwtPerson;
import com.sublinks.sublinksapi.api.lemmy.v3.community.mappers.GetCommunityResponseMapper;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityModeratorView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.CommunityView;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.FollowCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.GetCommunity;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.GetCommunityResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.ListCommunities;
import com.sublinks.sublinksapi.api.lemmy.v3.community.models.ListCommunitiesResponse;
import com.sublinks.sublinksapi.api.lemmy.v3.community.services.LemmyCommunityService;
import com.sublinks.sublinksapi.community.Community;
import com.sublinks.sublinksapi.community.CommunityRepository;
import com.sublinks.sublinksapi.instance.LocalInstanceContext;
import com.sublinks.sublinksapi.person.LinkPersonCommunity;
import com.sublinks.sublinksapi.person.LinkPersonCommunityRepository;
import com.sublinks.sublinksapi.person.Person;
import com.sublinks.sublinksapi.person.PersonRepository;
import com.sublinks.sublinksapi.person.enums.LinkPersonCommunityType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/api/v3/community")
public class CommunityController {
    private final LocalInstanceContext localInstanceContext;
    private final CommunityRepository communityRepository;
    private final LinkPersonCommunityRepository linkPersonCommunityRepository;
    private final PersonRepository personRepository;
    private final LemmyCommunityService lemmyCommunityService;
    private final GetCommunityResponseMapper getCommunityResponseMapper;


    public CommunityController(
            LocalInstanceContext localInstanceContext,
            CommunityRepository communityRepository,
            LinkPersonCommunityRepository linkPersonCommunityRepository, PersonRepository personRepository, LemmyCommunityService lemmyCommunityService,
            GetCommunityResponseMapper getCommunityResponseMapper
    ) {
        this.localInstanceContext = localInstanceContext;
        this.communityRepository = communityRepository;
        this.linkPersonCommunityRepository = linkPersonCommunityRepository;
        this.personRepository = personRepository;
        this.lemmyCommunityService = lemmyCommunityService;
        this.getCommunityResponseMapper = getCommunityResponseMapper;
    }


    @GetMapping
    public GetCommunityResponse show(@Valid GetCommunity getCommunityForm, JwtPerson principal) {
        Community community = communityRepository.findCommunityByIdOrTitleSlug(
                getCommunityForm.id(), getCommunityForm.name()
        );
        CommunityView communityView;
        if (principal != null) {
            Person person = (Person) principal.getPrincipal();
            communityView = lemmyCommunityService.communityViewFromCommunity(community, person);
        } else {
            communityView = lemmyCommunityService.communityViewFromCommunity(community);
        }
        Set<String> languageCodes = lemmyCommunityService.communityLanguageCodes(community);
        List<CommunityModeratorView> moderatorViews = lemmyCommunityService.communityModeratorViewList(community);
        return getCommunityResponseMapper.map(
                communityView,
                languageCodes,
                moderatorViews,
                localInstanceContext
        );
    }

    @GetMapping("list")
    @Transactional
    public ListCommunitiesResponse list(@Valid ListCommunities listCommunitiesForm, JwtPerson principal) {
        Collection<CommunityView> communityViews = new HashSet<>();

        Collection<Community> communities = communityRepository.findAll(); // @todo apply filters
        for (Community community : communities) {
            CommunityView communityView;
            if (principal != null) {
                Person person = (Person) principal.getPrincipal();
                communityView = lemmyCommunityService.communityViewFromCommunity(community, person);
            } else {
                communityView = lemmyCommunityService.communityViewFromCommunity(community);
            }
            communityViews.add(communityView);
        }

        return ListCommunitiesResponse.builder()
                .communities(communityViews)
                .build();
    }

    @PostMapping("follow")
    CommunityResponse follow(@Valid @RequestBody FollowCommunity followCommunityForm, JwtPerson principal) {
        Person person = (Person) principal.getPrincipal();

        Optional<Community> community = communityRepository.findById((long) followCommunityForm.community_id());

        if (community.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Optional<LinkPersonCommunity> linkPersonCommunity =
                linkPersonCommunityRepository.getLinkPersonCommunityByCommunityAndPersonAndLinkType(
                        community.get(),
                        person,
                        LinkPersonCommunityType.follower
                );

        if (!followCommunityForm.follow() && linkPersonCommunity.isPresent()) {
            linkPersonCommunityRepository.delete(linkPersonCommunity.get());
            person.getLinkPersonCommunity().removeIf(l -> Objects.equals(l.getId(), linkPersonCommunity.get().getId()));
        } else if (followCommunityForm.follow() && linkPersonCommunity.isEmpty()) {
            LinkPersonCommunity newLink = LinkPersonCommunity.builder()
                    .community(community.get())
                    .person(person)
                    .linkType(LinkPersonCommunityType.follower)
                    .build();
            linkPersonCommunityRepository.save(newLink);
            person.getLinkPersonCommunity().add(newLink);
        }
        return lemmyCommunityService.createCommunityResponse(
                community.get(),
                person
        );
    }
}
