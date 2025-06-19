package com.SoccerNode.PlayersTeams.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class PlayerTeamResponseDTO {

    @Data
    @Document(collection = "Player-Team")
    public static class FlattenedTeam {
        @Id
        private ObjectId _id;

        private Integer player;
        private Integer team;
        private String seasons;
    }

    private List<TeamsEntry> response;

    @Data
    public static class TeamsEntry {
        private Team team;
        private List<String> seasons;
    }

    @Data
    public static class Team {
        private Integer id;
    }

}
