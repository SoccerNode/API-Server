package com.SoccerNode.FixturesEvents.Datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class FixtureEventResponseDTO {

    @Data
    @Document(collection = "Fixture-Event")
    public static class FlattenedFixtureEvent {
        @Id
        private ObjectId _id;

        private Integer fixture;
        private Integer time;
        private Integer team;
        private Integer player;
        private Integer assist;
        private String type;
        private String detail;
        private String comments;
    }

    private List<FixtureEventEntry> response;

    @Data
    @AllArgsConstructor
    public static class FixtureEventEntry {
        private Time time;
        private Team team;
        private Player player;
        private Assist assist;
        private String type;
        private String detail;
        private String comments;
    }

    @Data
    public static class Time {
        private Integer elapsed;
        private Integer extra;
    }

    @Data
    public static class Team {
        private Integer id;
    }

    @Data
    public static class Player {
        private Integer id;
    }

    @Data
    public static class Assist {
        private Integer id;
    }

}
