package com.SoccerNode.FixturesLineups.Controller;

import com.SoccerNode.FixturesLineups.Datas.FixtureLineupRepository;
import com.SoccerNode.FixturesLineups.Datas.FixtureLineupResponseDTO;
import com.SoccerNode.FixturesLineups.Datas.FixtureLineupResponseDTO.*;
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
@RequestMapping("/fixtures/lineups")
public class GetFixturesLineups {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final FixtureLineupRepository fixtureLineupRepository;

    @Autowired
    public GetFixturesLineups(WebClient client, MongoTemplate mongoTemplate, FixtureLineupRepository fixtureLineupRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.fixtureLineupRepository = fixtureLineupRepository;
    }

    @PostMapping()
    public String getFixturesLineups(@RequestParam("fixture") int fixture) {
        Mono<FixtureLineupResponseDTO> mono = getResponse(fixture);
        Flux<FlattenedFixtureLineup> statistics = getFlattenedViaDTO(mono, fixture);

        statistics
                .map(fixtureLineupRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateFixturesLineups();
        return "Cleaned up";
    }

    public void deduplicateFixturesLineups() {
        List<FlattenedFixtureLineup> all = mongoTemplate.findAll(FlattenedFixtureLineup.class);
        Map<String, List<FlattenedFixtureLineup>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture() + "|" + l.getTeam() + "|" + l.getFormation() + "|" + l.getPlayer() + "|" + l.getPos() + "|" + l.getGrid()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedFixtureLineup> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedFixtureLineup l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedFixtureLineup::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedFixtureLineup.class);
        }
    }

    public Mono<FixtureLineupResponseDTO> getResponse(int fixture) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/lineups")
                        .queryParam("fixture", fixture)
                        .build())
                .retrieve()
                .bodyToMono(FixtureLineupResponseDTO.class);
    }

    public Flux<FixtureLineupResponseDTO.FlattenedFixtureLineup> getFlattenedViaDTO(Mono<FixtureLineupResponseDTO> mono, int fixture) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedFixtureEvent(entry, fixture));
    }

    private Flux<FixtureLineupResponseDTO.FlattenedFixtureLineup> toFlattenedFixtureEvent(FixtureLineupResponseDTO.FixtureLineupEntry entry, int fixture) {
        int team = entry.getTeam().getId();
        String formation = entry.getFormation();

        return Flux.fromIterable(entry.getStartXI())
                .map(startXI -> toFixtureLineup(startXI, fixture, team, formation));
    }

    private FixtureLineupResponseDTO.FlattenedFixtureLineup toFixtureLineup(FixtureLineupResponseDTO.StartXI startXI, int fixture, int team, String formation) {
        FixtureLineupResponseDTO.FlattenedFixtureLineup lineup = new FixtureLineupResponseDTO.FlattenedFixtureLineup();
        FixtureLineupResponseDTO.Player player = startXI.getPlayer();
        lineup.setFixture(fixture);
        lineup.setTeam(team);
        lineup.setFormation(formation);
        lineup.setPlayer(player.getId());
        lineup.setPos(player.getPos());
        lineup.setGrid(player.getGrid());

        return lineup;
    }

}
