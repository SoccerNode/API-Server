package com.SoccerNode.Injuries.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class InjuryResponseDTO {
    private List<InjuryEntry> response;

    @Data
    @Document(collection = "Injury")
    public static class FlattenedInjury {
        @Id
        private ObjectId _id;

        private int fixture;
        private int player;
        private String type;
        private String reason;
    }

    @Data
    public static class InjuryEntry {
        private Player player;
    }

    @Data
    public static class Player {
        private int id;
        private String type;
        private String reason;
    }

}
