package com.SoccerNode.Fixtures.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class FixtureResponseDTO {

    @Data
    @Document(collection = "Fixture")
    public static class FlattenedFixture {
        @Id
        private ObjectId _id;

        private Integer fixture;
        private Integer league;
        private Integer season;
        private Integer home;
        private Integer away;
        private String date;
        private Integer venue;
        private Integer homeScoreHalftime;
        private Integer awayScoreHalftime;
        private Integer homeScoreFulltime;
        private Integer awayScoreFulltime;
        private Integer homeScoreExtratime;
        private Integer awayScoreExtratime;
        private Integer homeScorePenalty;
        private Integer awayScorePenalty;
    }

    private List<FixtureEntry> response;

    @Data
    public static class FixtureEntry {
        private Fixture fixture;
        private League league;
        private Teams teams;
        private Score score;

    }

    @Data
    public static class Fixture {
        private int id;
        private String date;
        private Venue venue;
    }

    @Data
    public static class Venue {
        private int id;
    }

    @Data
    public static class League {
        private int id; // Optional: leagueId + "_" + year
        private int season;
    }

    @Data
    public static class Teams {
        private Team home;
        private Team away;
    }

    @Data
    public static class Team {
        private int id;
    }

    @Data
    public static class Score {
        private Goal halftime;
        private Goal fulltime;
        private Goal extratime;
        private Goal penalty;
    }

    @Data
    public static class Goal {
        private Integer home;
        private Integer away;
    }

}
