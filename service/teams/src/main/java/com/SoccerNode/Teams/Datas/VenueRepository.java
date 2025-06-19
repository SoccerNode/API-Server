package com.SoccerNode.Teams.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VenueRepository extends MongoRepository<TeamResponseDTO.Venue, String> {
    // 추가 쿼리 메서드 정의 가능
}
