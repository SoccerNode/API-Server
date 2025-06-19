package com.SoccerNode.Fixtures.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FixtureRepository extends MongoRepository<FixtureResponseDTO.FlattenedFixture, String> {
    // 추가 쿼리 메서드 정의 가능
}
