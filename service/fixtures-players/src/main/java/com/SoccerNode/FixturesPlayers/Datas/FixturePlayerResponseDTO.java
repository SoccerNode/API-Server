package com.SoccerNode.FixturesPlayers.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class FixturePlayerResponseDTO {

    @Data
    @Document(collection = "Fixture-Player")
    public static class FlattenedFixturePlayer {
        @Id
        private ObjectId _id;

        private Integer fixture;
        private Integer team;
        private Integer player;
        private Integer minutes;
        private Integer number;
        private String position;
        private String rating;
        private Boolean captain;
        private Boolean substitute;
        private Integer offsides;
        private Integer shots;
        private Integer shotsOn;
        private Integer goals;
        private Integer goalsConceded;
        private Integer goalsAssists;
        private Integer goalsSaves;
        private Integer passes;
        private Integer passesKey;
        private String passesAccuracy;
        private Integer tackles;
        private Integer tacklesBlocks;
        private Integer tacklesInterceptions;
        private Integer duels;
        private Integer duelsWon;
        private Integer dribblesAttempts;
        private Integer dribblesSuccess;
        private Integer dribblesPast;
        private Integer foulsDrawn;
        private Integer foulsCommitted;
        private Integer cardsYellow;
        private Integer cardsRed;
        private Integer penaltyWon;
        private Integer penaltyCommited;
        private Integer penaltyScored;
        private Integer penaltyMissed;
        private Integer penaltySaved;
    }

    private List<FixturePlayerEntry> response;

    @Data
    public static class FixturePlayerEntry {
        private Team team;
        private List<Players> players;
    }

    @Data
    public static class Team {
        private Integer id;
    }

    @Data
    public static class Players {
        private Player player;
        private List<Statistics> statistics;
    }

    @Data
    public static class Player {
        private Integer id;
        private String pos;
        private String grid;
    }

    @Data
    public static class Statistics {
        private Games games;
        private Integer offsides;
        private Shots shots;
        private Goals goals;
        private Passes passes;
        private Tackles tackles;
        private Duels duels;
        private Dribbles dribbles;
        private Fouls fouls;
        private Cards cards;
        private Penalty penalty;
    }

    @Data
    public static class Games {
        private Integer minutes;
        private Integer number;
        private String position;
        private String rating;
        private Boolean captain;
        private Boolean substitute;
    }

    @Data
    public static class Shots {
        private Integer total;
        private Integer on;
    }

    @Data
    public static class Goals {
        private Integer total;
        private Integer conceded;
        private Integer assists;
        private Integer saves;
    }

    @Data
    public static class Passes {
        private Integer total;
        private Integer key;
        private String accuracy;
    }

    @Data
    public static class Tackles {
        private Integer total;
        private Integer blocks;
        private Integer interceptions;
    }

    @Data
    public static class Duels {
        private Integer total;
        private Integer won;
    }

    @Data
    public static class Dribbles {
        private Integer attempts;
        private Integer success;
        private Integer past;
    }

    @Data
    public static class Fouls {
        private Integer drawn;
        private Integer committed;
    }

    @Data
    public static class Cards {
        private Integer yellow;
        private Integer red;
    }

    @Data
    public static class Penalty {
        private Integer won;
        private Integer commited;
        private Integer scored;
        private Integer missed;
        private Integer saved;
    }

}
