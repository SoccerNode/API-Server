package com.SoccerNode.PlayersSquads.Datas;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PlayerSquadRepository extends ReactiveMongoRepository<PlayerSquadResponseDTO.FlattenedSquad, String> {
    // 추가 쿼리 메서드 정의 가능
}
