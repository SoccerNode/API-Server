package com.SoccerNode.Leagues.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class LeagueResponseDTO {
    private List<LeagueEntry> response;

    @Data
    public static class LeagueEntry {
        private League league;
        private List<Season> seasons;
    }

    @Data
    @Document(collection = "League")
    public static class League {
        @Id
        private ObjectId _id;

        private int id;
        private String name;
        private String type;
        private String logo;
    }

    @Data
    @Document(collection = "Season")
    public static class FlattenedSeason {
        @Id
        private ObjectId _id;

        private int league;
        private int year;
        private String start;
        private String end;
    }

    @Data
    public static class Season {
        private int year;
        private String start;
        private String end;
    }

}
