package com.SoccerNode.Sidelined.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoachSidelinedRepository extends MongoRepository<SidelinedResponseDTO.FlattenedCoachSidelined, String> {
    // 추가 쿼리 메서드 정의 가능
}
