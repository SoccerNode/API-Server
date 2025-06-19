package com.SoccerNode.TeamsStatistics.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamStatisticRepository extends MongoRepository<TeamStatisticResponseDTO.FlattenedTeamStatistic, String> {
    // 추가 쿼리 메서드 정의 가능
}
