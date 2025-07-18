package com.example.url_media_save.db.mapper;

import com.example.url_media_save.db.entity.UrlRequestEntity;
import com.example.url_media_save.dto.UrlRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlRequestEntity toEntity(UrlRequestDto dto);
    UrlRequestDto toDto(UrlRequestEntity entity);

    void updateResultFields(@MappingTarget UrlRequestEntity entity,
                            Long requiredTimeMs,
                            Integer deleteDuplicates,
                            Integer totalProcessed,
                            Integer totalSuccessful,
                            Integer totalFailed);
}
