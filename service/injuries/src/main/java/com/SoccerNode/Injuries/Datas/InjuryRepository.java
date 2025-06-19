package com.SoccerNode.Injuries.Datas;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InjuryRepository extends MongoRepository<InjuryResponseDTO.FlattenedInjury, String> {
    // 추가 쿼리 메서드 정의 가능
}
