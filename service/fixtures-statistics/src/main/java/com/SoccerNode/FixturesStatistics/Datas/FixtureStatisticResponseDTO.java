package com.SoccerNode.FixturesStatistics.Datas;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
public class FixtureStatisticResponseDTO {

    @Data
    @Document(collection = "Fixture-Statistic")
    public static class FlattenedFixtureStatistic {
        @Id
        private ObjectId _id;

        private int fixture;
        private int team;
        private String type;
        private String value;
    }

    private List<FixtureStatisticEntry> response;

    @Data
    public static class FixtureStatisticEntry {
        private Team team;
        private List<Statistic> statistics;
    }

    @Data
    public static class Team {
        private int id;
    }

    @Data
    public static class Statistic {
        private String type;
        private String value;
    }

}
