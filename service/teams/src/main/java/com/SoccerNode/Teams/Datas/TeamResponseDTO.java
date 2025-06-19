package com.SoccerNode.Teams.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class TeamResponseDTO {
    private List<TeamEntry> response;

    @Data
    public static class TeamEntry {
        private Team team;
        private Venue venue;
    }

    @Data
    @Document(collection = "Team")
    public static class Team {
        @Id
        private ObjectId _id;

        private int id;
        private String name;
        private String code;
        private String country;
        private int founded;
        private String logo;

    }

    @Data
    @Document(collection = "Venue")
    public static class Venue {
        @Id
        private ObjectId _id;

        private int id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;

    }
}
