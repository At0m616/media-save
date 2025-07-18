package com.example.url_media_save.db.mapper;

import com.example.url_media_save.db.entity.UrlRequestEntity;
import com.example.url_media_save.dto.UrlRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    UrlRequestEntity toUrlModel(UrlRequestDto urlRequestDto);
}
