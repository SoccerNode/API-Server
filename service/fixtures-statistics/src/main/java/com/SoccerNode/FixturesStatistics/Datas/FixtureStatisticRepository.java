package com.SoccerNode.FixturesStatistics.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FixtureStatisticRepository extends MongoRepository<FixtureStatisticResponseDTO.FlattenedFixtureStatistic, String> {
    // 추가 쿼리 메서드 정의 가능
}
