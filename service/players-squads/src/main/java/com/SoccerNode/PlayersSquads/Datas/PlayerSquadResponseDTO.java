package com.SoccerNode.PlayersSquads.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class PlayerSquadResponseDTO {

    @Data
    @Document(collection = "Squad")
    public static class FlattenedSquad {
        @Id
        private ObjectId _id;

        private Integer team;
        private Integer player;
        private Integer number;
        private String position;
    }

    private List<TeamSquadEntry> response;

    @Data
    public static class TeamSquadEntry {
        private Team team;
        private List<Player> players;
    }

    @Data
    public static class Team {
        private Integer id;
    }

    @Data
    public static class Player {
        private Integer id;
        private Integer number;
        private String position;
    }

}
