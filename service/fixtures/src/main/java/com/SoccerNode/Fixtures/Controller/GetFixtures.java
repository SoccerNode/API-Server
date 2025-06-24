package com.SoccerNode.Fixtures.Controller;

import com.SoccerNode.Fixtures.Datas.FixtureRepository;
import com.SoccerNode.Fixtures.Datas.FixtureResponseDTO;
import com.SoccerNode.Fixtures.Datas.FixtureResponseDTO.*;
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
@RequestMapping("/fixtures")
public class GetFixtures {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final FixtureRepository fixtureRepository;

    @Autowired
    public GetFixtures(WebClient client, MongoTemplate mongoTemplate, FixtureRepository fixtureRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.fixtureRepository = fixtureRepository;
    }

    @PostMapping()
    public String getFixtures(@RequestParam("league") int league, @RequestParam("season") int season) {
        Mono<FixtureResponseDTO> mono = getResponse(league, season);
        Flux<FlattenedFixture> fixtures = getFlattenedViaDTO(mono);

        fixtures
                .map(fixtureRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateFixtures();
        return "Cleaned up";
    }

    public void deduplicateFixtures() {
        List<FlattenedFixture> all = mongoTemplate.findAll(FlattenedFixture.class);
        Map<String, List<FlattenedFixture>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture().toString()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedFixture> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedFixture l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedFixture::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedFixture.class);
        }
    }

    public Mono<FixtureResponseDTO> getResponse(int league, int season) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("league", league)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(FixtureResponseDTO.class);
    }

    public Flux<FixtureResponseDTO.FlattenedFixture> getFlattenedViaDTO(Mono<FixtureResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(this::toFlattenedFixture);
    }

    private FixtureResponseDTO.FlattenedFixture toFlattenedFixture(FixtureResponseDTO.FixtureEntry entry) {
        FixtureResponseDTO.FlattenedFixture fixture = new FixtureResponseDTO.FlattenedFixture();
        fixture.setFixture(entry.getFixture().getId());
        fixture.setLeague(entry.getLeague().getId());
        fixture.setSeason(entry.getLeague().getSeason());
        fixture.setHome(entry.getTeams().getHome().getId());
        fixture.setAway(entry.getTeams().getAway().getId());
        fixture.setDate(entry.getFixture().getDate());
        fixture.setVenue(entry.getFixture().getVenue().getId());
        fixture.setHomeScoreHalftime(entry.getScore().getHalftime().getHome());
        fixture.setAwayScoreHalftime(entry.getScore().getHalftime().getAway());
        fixture.setHomeScoreFulltime(entry.getScore().getFulltime().getHome());
        fixture.setAwayScoreFulltime(entry.getScore().getFulltime().getAway());
        fixture.setHomeScoreExtratime(entry.getScore().getExtratime().getHome());
        fixture.setAwayScoreExtratime(entry.getScore().getExtratime().getAway());
        fixture.setHomeScorePenalty(entry.getScore().getPenalty().getHome());
        fixture.setAwayScorePenalty(entry.getScore().getPenalty().getAway());
        return fixture;
    }

}
