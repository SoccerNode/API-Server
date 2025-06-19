package com.SoccerNode.PlayersTeams.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerTeamRepository extends MongoRepository<PlayerTeamResponseDTO.FlattenedTeam, String> {
    // 추가 쿼리 메서드 정의 가능
}
