package com.SoccerNode.Trophies.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoachTrophyRepository extends MongoRepository<TrophyResponseDTO.FlattenedCoachTrophy, String> {
    // 추가 쿼리 메서드 정의 가능
}
