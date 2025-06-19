package com.SoccerNode.Standings.Datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingResponseDTO {

    @Data
    @Document(collection = "Standing")
    public static class FlattenedStanding {
        @Id
        private ObjectId _id;

        private Integer league;
        private Integer season;
        private Integer team;
        private Integer points;
        private Integer goalsDiff;
        private String form;
        private String status;
        private String description;
    }

    private List<StandingEntry> response;

    @Data
    public static class StandingEntry {
        private League league;  // league.id
    }

    @Data
    public static class League {
        private Integer id; // Optional: leagueId + "_" + year
        private Integer season;
        private List<List<Standings>> standings;
    }

    @Data
    public static class Standings {
        private Integer rank;
        private Team team;
        private Integer points;
        private Integer goalsDiff;
        private String form;
        private String status;
        private String description;
    }

    @Data
    public static class Team {
        private Integer id;
    }

}
