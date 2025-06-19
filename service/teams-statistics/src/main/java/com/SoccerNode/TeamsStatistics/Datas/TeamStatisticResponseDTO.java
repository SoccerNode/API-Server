package com.SoccerNode.TeamsStatistics.Datas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatisticResponseDTO {

    @Data
    @Document(collection = "Team-Statistic")
    public static class FlattenedTeamStatistic {
        @Id
        private ObjectId _id;

        private Integer league;
        private Integer season;
        private Integer team;
        private String form;

        private Integer homePlayed;
        private Integer homeWin;
        private Integer homeGoal;

        private Integer awayPlayed;
        private Integer awayWin;
        private Integer awayGoal;

        private Integer penaltyCount;
        private Integer penaltyScored;

        private Integer cardsYellow60;
        private Integer cardsYellow120;
        private Integer cardsRed60;
        private Integer cardsRed120;
    }

    private TeamStatisticEntry response;

    @Data
    public static class TeamStatisticEntry {
        private League league;  // league.id
        private Team team;
        private String form;
        private Fixtures fixtures;
        private Goals goals;
        private Penalty penalty;
        private Cards cards;
    }

    @Data
    public static class League {
        private Integer id; // Optional: leagueId + "_" + year
        private Integer season;
    }

    @Data
    public static class Team {
        private Integer id;
    }

    @Data
    public static class Fixtures {
        private Played played;
        private Wins wins;
    }

    @Data
    public static class Played {
        private Integer home;
        private Integer away;
    }

    @Data
    public static class Wins {
        private Integer home;
        private Integer away;
    }

    @Data
    public static class Goals {
        @JsonProperty("for")
        private For _for;
    }

    @Data
    public static class For {
        private Total total;
    }

    @Data
    public static class Total {
        private Integer home;
        private Integer away;
    }

    @Data
    public static class Penalty {
        private Integer total;
        private Scored scored;
    }

    @Data
    public static class Scored {
        private Integer total;
    }

    @Data
    public static class Cards {
        private Yellow yellow;
        private Red red;
    }

    @Data
    public static class Yellow {
        @JsonProperty("0-15")
        private Card time15;
        @JsonProperty("16-30")
        private Card time30;
        @JsonProperty("31-45")
        private Card time45;
        @JsonProperty("46-60")
        private Card time60;
        @JsonProperty("61-75")
        private Card time75;
        @JsonProperty("76-90")
        private Card time90;
        @JsonProperty("91-105")
        private Card time105;
        @JsonProperty("106-120")
        private Card time120;
    }

    @Data
    public static class Red {
        @JsonProperty("0-15")
        private Card time15;
        @JsonProperty("16-30")
        private Card time30;
        @JsonProperty("31-45")
        private Card time45;
        @JsonProperty("46-60")
        private Card time60;
        @JsonProperty("61-75")
        private Card time75;
        @JsonProperty("76-90")
        private Card time90;
        @JsonProperty("91-105")
        private Card time105;
        @JsonProperty("106-120")
        private Card time120;
    }

    @Data
    public static class Card {
        private Integer total;
    }

}
