package com.SoccerNode.FixturesPlayers.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FixturePlayerRepository extends MongoRepository<FixturePlayerResponseDTO.FlattenedFixturePlayer, String> {
    // 추가 쿼리 메서드 정의 가능
}
