package com.SoccerNode.Coaches.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class CoachResponseDTO {

    @Data
    @Document(collection = "Coach")
    public static class FlattenedCoach {
        @Id
        private ObjectId _id;

        private Integer id;
        private String name;
        private String firstname;
        private String lastname;
        private String birth;
        private String hometown;
        private String country;
        private String nationality;
        private String height;
        private String weight;
        private String photo;
    }

    @Data
    @Document(collection = "Career")
    public static class FlattenedCareer {
        @Id
        private ObjectId _id;

        private Integer coach;
        private Integer team;
        private String start;
        private String end;
    }

    private List<CoachEntry> response;

    @Data
    public static class CoachEntry {
        private Integer id;
        private String name;
        private String firstname;
        private String lastname;
        private Birth birth;
        private String nationality;
        private String height;
        private String weight;
        private String photo;
        private List<Career> career;
    }

    @Data
    public static class Birth {
        private String date;
        private String place;
        private String country;
    }

    @Data
    public static class Career {
        private Team team;
        private String start;
        private String end;
    }

    @Data
    public static class Team {
        private Integer id;
    }

}
