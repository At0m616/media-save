package com.example.url_media_save.db.repository;

import com.example.url_media_save.db.entity.UrlRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<UrlRequestEntity, Long> {
}
