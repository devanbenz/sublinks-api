package com.sublinks.sublinksapi.api.lemmy.v3.admin.mapper;

import com.sublinks.sublinksapi.api.lemmy.v3.admin.models.AdminPurgeCommunity;
import com.sublinks.sublinksapi.moderation.dto.ModerationLog;
import com.sublinks.sublinksapi.utils.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminPurgeCommunityMapper extends Converter<ModerationLog, AdminPurgeCommunity> {

  @Override
  @Mapping(target = "id", source = "moderationLog.id")
  @Mapping(target = "when_", source = "moderationLog.createdAt",
      dateFormat = DateUtils.FRONT_END_DATE_FORMAT)
  @Mapping(target = "admin_person_id", source = "moderationLog.adminPersonId")
  @Mapping(target = "reason", source = "moderationLog.reason")
  AdminPurgeCommunity convert(@Nullable ModerationLog moderationLog);
}
