package com.SoccerNode.Trophies.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerTrophyRepository extends MongoRepository<TrophyResponseDTO.FlattenedPlayerTrophy, String> {
    // 추가 쿼리 메서드 정의 가능
}
