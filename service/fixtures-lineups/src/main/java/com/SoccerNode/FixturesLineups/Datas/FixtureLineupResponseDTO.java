package com.SoccerNode.FixturesLineups.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class FixtureLineupResponseDTO {

    @Data
    @Document(collection = "Fixture-Lineup")
    public static class FlattenedFixtureLineup {
        @Id
        private ObjectId _id;

        private Integer fixture;
        private Integer team;
        private String formation;
        private Integer player;
        private String pos;
        private String grid;
    }

    private List<FixtureLineupEntry> response;

    @Data
    public static class FixtureLineupEntry {
        private Team team;
        private String formation;
        private List<StartXI> startXI;
    }

    @Data
    public static class Team {
        private Integer id;
    }

    @Data
    public static class StartXI {
        private Player player;
    }

    @Data
    public static class Player {
        private Integer id;
        private String pos;
        private String grid;
    }

}
