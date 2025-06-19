package com.SoccerNode.Coaches.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoachRepository extends MongoRepository<CoachResponseDTO.FlattenedCoach, String> {
    // 추가 쿼리 메서드 정의 가능
}
