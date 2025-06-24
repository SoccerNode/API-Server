package com.SoccerNode.Leagues.Controller;

import com.SoccerNode.Leagues.Datas.LeagueRepository;
import com.SoccerNode.Leagues.Datas.LeagueResponseDTO;
import com.SoccerNode.Leagues.Datas.LeagueResponseDTO.*;
import com.SoccerNode.Leagues.Datas.SeasonRepository;
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
@RequestMapping("/leagues")
public class GetLeagues {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final LeagueRepository leagueRepository;
    private final SeasonRepository seasonRepository;

    @Autowired
    public GetLeagues(WebClient client, MongoTemplate mongoTemplate, LeagueRepository leagueRepository, SeasonRepository seasonRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.leagueRepository = leagueRepository;
        this.seasonRepository = seasonRepository;
    }

    @PostMapping()
    public String getLeagues(@RequestParam("season") int season) {
        Mono<LeagueResponseDTO> mono = getResponse(season);
        Flux<League> leagues = getLeaguesViaDTO(mono);
        Flux<FlattenedSeason> seasons = getSeasonsViaDTO(mono);

        leagues
                .map(leagueRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        seasons
                .map(seasonRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateLeagues();
        deduplicateSeasons();
        return "Cleaned up";
    }

    public void deduplicateLeagues() {
        List<League> all = mongoTemplate.findAll(League.class);
        Map<String, List<League>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getName() + "|" + l.getType() + "|" + l.getLogo()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<League> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((League l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(League::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, League.class);
        }
    }

    public void deduplicateSeasons() {
        List<FlattenedSeason> all = mongoTemplate.findAll(FlattenedSeason.class);
        Map<String, List<FlattenedSeason>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getLeague() + "|" + l.getYear()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedSeason> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedSeason l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedSeason::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, League.class);
        }
    }

    public Mono<LeagueResponseDTO> getResponse(int season) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/leagues")
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(LeagueResponseDTO.class);
    }

    public Flux<League> getLeaguesViaDTO(Mono<LeagueResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(LeagueEntry::getLeague);
    }

    public Flux<FlattenedSeason> getSeasonsViaDTO(Mono<LeagueResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(this::flattenEntryToSeasons);
    }

    private Flux<FlattenedSeason> flattenEntryToSeasons(LeagueEntry entry) {
        int leagueId = entry.getLeague().getId();
        return Flux.fromIterable(entry.getSeasons())
                .map(season -> toSeason(season, leagueId));
    }

    private FlattenedSeason toSeason(Season season, int leagueId) {

        FlattenedSeason flattened = new FlattenedSeason();
        flattened.setLeague(leagueId);
        flattened.setYear(season.getYear());
        flattened.setStart(season.getStart());
        flattened.setEnd(season.getEnd());

        return flattened;
    }

}
