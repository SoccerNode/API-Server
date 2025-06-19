package com.SoccerNode.Leagues.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeagueRepository extends MongoRepository<LeagueResponseDTO.League, String> {
    // 추가 쿼리 메서드 정의 가능
}
