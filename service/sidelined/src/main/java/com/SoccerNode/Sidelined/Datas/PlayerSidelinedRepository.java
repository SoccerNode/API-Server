package com.SoccerNode.Sidelined.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerSidelinedRepository extends MongoRepository<SidelinedResponseDTO.FlattenedPlayerSidelined, String> {
    // 추가 쿼리 메서드 정의 가능
}
