package com.SoccerNode.FixturesStatistics.Controller;

import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticRepository;
import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticResponseDTO;
import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticResponseDTO.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fixtures/statistics")
public class GetFixtureStatistic {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final FixtureStatisticRepository fixtureStatisticRepository;

    @Autowired
    public GetFixtureStatistic(WebClient client, MongoTemplate mongoTemplate, FixtureStatisticRepository fixtureStatisticRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.fixtureStatisticRepository = fixtureStatisticRepository;
    }

    @PostMapping()
    public String getFixturesStatistics(@RequestParam("fixture") int fixture) {
        Mono<FixtureStatisticResponseDTO> mono = getResponse(fixture);
        Flux<FlattenedFixtureStatistic> statistics = getFlattenedViaDTO(mono, fixture);

        statistics
                .map(fixtureStatisticRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateFixturesStatistics();
        return "Cleaned up";
    }

    public void deduplicateFixturesStatistics() {
        List<FlattenedFixtureStatistic> all = mongoTemplate.findAll(FlattenedFixtureStatistic.class);
        Map<String, List<FlattenedFixtureStatistic>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture() + "|" + l.getTeam() + "|" + l.getType() + " | " + l.getValue()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedFixtureStatistic> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedFixtureStatistic l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedFixtureStatistic::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedFixtureStatistic.class);
        }
    }

    public Mono<FixtureStatisticResponseDTO> getResponse(int fixture) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/statistics")
                        .queryParam("fixture", fixture)
                        .build())
                .retrieve()
                .bodyToMono(FixtureStatisticResponseDTO.class);
    }

    public Flux<FixtureStatisticResponseDTO.FlattenedFixtureStatistic> getFlattenedViaDTO(Mono<FixtureStatisticResponseDTO> mono, int fixture) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedFixtureStatistic(entry, fixture));
    }

    private Flux<FixtureStatisticResponseDTO.FlattenedFixtureStatistic> toFlattenedFixtureStatistic(FixtureStatisticResponseDTO.FixtureStatisticEntry entry, int fixture) {
        int team = entry.getTeam().getId();
        return Flux.fromIterable(entry.getStatistics())
                .map(statistic -> toStatistics(statistic, team, fixture));
    }

    private FixtureStatisticResponseDTO.FlattenedFixtureStatistic toStatistics(FixtureStatisticResponseDTO.Statistic statistic, int team, int fixture) {
        FixtureStatisticResponseDTO.FlattenedFixtureStatistic flat = new FixtureStatisticResponseDTO.FlattenedFixtureStatistic();
        flat.setFixture(fixture);
        flat.setTeam(team);
        flat.setType(statistic.getType());
        flat.setValue(statistic.getValue());
        return flat;
    }
    
}
