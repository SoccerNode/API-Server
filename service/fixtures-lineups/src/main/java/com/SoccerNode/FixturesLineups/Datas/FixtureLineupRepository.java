package com.SoccerNode.FixturesLineups.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FixtureLineupRepository extends MongoRepository<FixtureLineupResponseDTO.FlattenedFixtureLineup, String> {
    // 추가 쿼리 메서드 정의 가능
}
