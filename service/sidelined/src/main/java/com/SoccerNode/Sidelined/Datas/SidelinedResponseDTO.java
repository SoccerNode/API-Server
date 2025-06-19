package com.SoccerNode.Sidelined.Datas;

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
public class SidelinedResponseDTO {

    @Data
    @Document(collection = "Player-Sidelined")
    public static class FlattenedPlayerSidelined {
        @Id
        private ObjectId _id;

        private Integer player;
        private String type;
        private String start;
        private String end;
    }

    @Data
    @Document(collection = "Coach-Sidelined")
    public static class FlattenedCoachSidelined {
        @Id
        private ObjectId _id;

        private Integer coach;
        private String type;
        private String start;
        private String end;
    }

    private List<SidelinedEntry> response;

    @Data
    @AllArgsConstructor
    public static class SidelinedEntry {
        private String type;
        private String start;
        private String end;
    }

}
