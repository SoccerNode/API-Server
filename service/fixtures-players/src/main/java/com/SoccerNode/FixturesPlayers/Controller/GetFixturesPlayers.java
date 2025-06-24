package com.SoccerNode.FixturesPlayers.Controller;

import com.SoccerNode.FixturesPlayers.Datas.FixturePlayerRepository;
import com.SoccerNode.FixturesPlayers.Datas.FixturePlayerResponseDTO;
import com.SoccerNode.FixturesPlayers.Datas.FixturePlayerResponseDTO.*;
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
@RequestMapping("/fixtures/player")
public class GetFixturesPlayers {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final FixturePlayerRepository fixturePlayerRepository;

    @Autowired
    public GetFixturesPlayers(WebClient client, MongoTemplate mongoTemplate, FixturePlayerRepository fixturePlayerRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.fixturePlayerRepository = fixturePlayerRepository;
    }

    @PostMapping()
    public String getFixturesPlayers(@RequestParam("fixture") int fixture) {
        Mono<FixturePlayerResponseDTO> mono = getResponse(fixture);
        Flux<FlattenedFixturePlayer> players = getFlattenedViaDTO(mono, fixture);

        players
                .map(fixturePlayerRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateFixturesPlayers();
        return "Cleaned up";
    }

    public void deduplicateFixturesPlayers() {
        List<FlattenedFixturePlayer> all = mongoTemplate.findAll(FlattenedFixturePlayer.class);
        Map<String, List<FlattenedFixturePlayer>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture() + "|" + l.getTeam() + "|" + l.getPlayer()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedFixturePlayer> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedFixturePlayer l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedFixturePlayer::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedFixturePlayer.class);
        }
    }

    public Mono<FixturePlayerResponseDTO> getResponse(int fixture) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/players")
                        .queryParam("fixture", fixture)
                        .build())
                .retrieve()
                .bodyToMono(FixturePlayerResponseDTO.class);
    }

    public Flux<FixturePlayerResponseDTO.FlattenedFixturePlayer> getFlattenedViaDTO(Mono<FixturePlayerResponseDTO> mono, int fixture) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedFixturePlayer(entry, fixture));
    }

    private Flux<FixturePlayerResponseDTO.FlattenedFixturePlayer> toFlattenedFixturePlayer(FixturePlayerResponseDTO.FixturePlayerEntry entry, int fixture) {
        int team = entry.getTeam().getId();

        return Flux.fromIterable(entry.getPlayers())
                .flatMap(players -> toFixturePlayer(fixture, team, players));
    }

    private Flux<FixturePlayerResponseDTO.FlattenedFixturePlayer> toFixturePlayer(int fixture, int team, FixturePlayerResponseDTO.Players players) {
        FixturePlayerResponseDTO.Player player = players.getPlayer();

        return Flux.fromIterable(players.getStatistics())
                .map(statistics -> toFixturePlayerStatistic(fixture, team, player, statistics));
    }

    private FixturePlayerResponseDTO.FlattenedFixturePlayer toFixturePlayerStatistic(int fixture, int team, FixturePlayerResponseDTO.Player player, FixturePlayerResponseDTO.Statistics statistics) {
        FixturePlayerResponseDTO.FlattenedFixturePlayer flat = new FixturePlayerResponseDTO.FlattenedFixturePlayer();
        flat.setFixture(fixture);
        flat.setTeam(team);
        flat.setPlayer(player.getId());
        flat.setMinutes(statistics.getGames().getMinutes());
        flat.setNumber(statistics.getGames().getNumber());
        flat.setPosition(statistics.getGames().getPosition());
        flat.setRating(statistics.getGames().getRating());
        flat.setCaptain(statistics.getGames().getCaptain());
        flat.setSubstitute(statistics.getGames().getSubstitute());
        flat.setOffsides(statistics.getOffsides());
        flat.setShots(statistics.getShots().getTotal());
        flat.setShotsOn(statistics.getShots().getOn());
        flat.setGoals(statistics.getGoals().getTotal());
        flat.setGoalsConceded(statistics.getGoals().getConceded());
        flat.setGoalsAssists(statistics.getGoals().getAssists());
        flat.setGoalsSaves(statistics.getGoals().getSaves());
        flat.setPasses(statistics.getPasses().getTotal());
        flat.setPassesKey(statistics.getPasses().getKey());
        flat.setPassesAccuracy(statistics.getPasses().getAccuracy());
        flat.setTackles(statistics.getTackles().getTotal());
        flat.setTacklesBlocks(statistics.getTackles().getBlocks());
        flat.setTacklesInterceptions(statistics.getTackles().getInterceptions());
        flat.setDuels(statistics.getDuels().getTotal());
        flat.setDuelsWon(statistics.getDuels().getWon());
        flat.setDribblesAttempts(statistics.getDribbles().getAttempts());
        flat.setDribblesSuccess(statistics.getDribbles().getSuccess());
        flat.setDribblesPast(statistics.getDribbles().getPast());
        flat.setFoulsDrawn(statistics.getFouls().getDrawn());
        flat.setFoulsCommitted(statistics.getFouls().getCommitted());
        flat.setCardsYellow(statistics.getCards().getYellow());
        flat.setCardsRed(statistics.getCards().getRed());
        flat.setPenaltyWon(statistics.getPenalty().getWon());
        flat.setPenaltyCommited(statistics.getPenalty().getCommited());
        flat.setPenaltyScored(statistics.getPenalty().getScored());
        flat.setPenaltyMissed(statistics.getPenalty().getMissed());
        flat.setPenaltySaved(statistics.getPenalty().getSaved());
        return flat;
    }
    
}
