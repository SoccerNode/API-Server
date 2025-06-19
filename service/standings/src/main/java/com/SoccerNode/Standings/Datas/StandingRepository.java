package com.SoccerNode.Standings.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StandingRepository extends MongoRepository<StandingResponseDTO.FlattenedStanding, String> {
    // 추가 쿼리 메서드 정의 가능
}
