package com.crio.rentvideo.repository;

import com.crio.rentvideo.entity.Video;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByAvailableTrue();
}
