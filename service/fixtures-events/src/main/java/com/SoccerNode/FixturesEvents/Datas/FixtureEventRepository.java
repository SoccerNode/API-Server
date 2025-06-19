package com.SoccerNode.FixturesEvents.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FixtureEventRepository extends MongoRepository<FixtureEventResponseDTO.FlattenedFixtureEvent, String> {
    // 추가 쿼리 메서드 정의 가능
}
