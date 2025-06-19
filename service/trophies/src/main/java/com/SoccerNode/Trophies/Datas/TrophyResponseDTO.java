package com.SoccerNode.Trophies.Datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class TrophyResponseDTO {

    @Data
    @Document(collection = "Player-Trophy")
    public static class FlattenedPlayerTrophy {
        @Id
        private ObjectId _id;

        private Integer player;
        private String league;
        private String country;
        private String season;
        private String place;
    }

    @Data
    @Document(collection = "Coach-Trophy")
    public static class FlattenedCoachTrophy {
        @Id
        private ObjectId _id;

        private Integer coach;
        private String league;
        private String country;
        private String season;
        private String place;
    }

    private List<TrophyEntry> response;

    @Data
    @AllArgsConstructor
    public static class TrophyEntry {
        private String league;
        private String country;
        private String season;
        private String place;
    }

}
