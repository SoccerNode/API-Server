package com.SoccerNode.PlayersSquads.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerSquadRepository extends MongoRepository<PlayerSquadResponseDTO.FlattenedSquad, String> {
    // 추가 쿼리 메서드 정의 가능
}
