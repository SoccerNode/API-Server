package com.SoccerNode.Standings.Controller;

import com.SoccerNode.Standings.Datas.StandingRepository;
import com.SoccerNode.Standings.Datas.StandingResponseDTO;
import com.SoccerNode.Standings.Datas.StandingResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/standings")
public class GetStandings {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final StandingRepository standingRepository;

    @Autowired
    public GetStandings(WebClient client, MongoTemplate mongoTemplate, StandingRepository standingRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.standingRepository = standingRepository;
    }

    @PostMapping()
    public String getStandings(@RequestParam("league") int league, @RequestParam("season") int season) {
        Mono<StandingResponseDTO> mono = getResponse(league, season);
        Flux<FlattenedStanding> standing = getFlattenedViaDTO(mono);

        standing
                .map(standingRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<StandingResponseDTO> getResponse(int league, int season) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/standings")
                        .queryParam("league", league)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(StandingResponseDTO.class);
    }

    public Flux<StandingResponseDTO.FlattenedStanding> getFlattenedViaDTO(Mono<StandingResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(this::flattenEntry);
    }

    private Flux<StandingResponseDTO.FlattenedStanding> flattenEntry(StandingResponseDTO.StandingEntry entry) {
        StandingResponseDTO.League league = entry.getLeague();
        Integer leagueId = league.getId();
        Integer season = league.getSeason();

        return Flux.fromIterable(league.getStandings())
                .flatMap(innerList -> Flux.fromIterable(innerList)
                        .map(s -> toFlattenedStanding(s, leagueId, season)));
    }

    private StandingResponseDTO.FlattenedStanding toFlattenedStanding(
            StandingResponseDTO.Standings standing, Integer leagueId, Integer season) {

        StandingResponseDTO.FlattenedStanding flat = new StandingResponseDTO.FlattenedStanding();
        flat.setLeague(leagueId);
        flat.setSeason(season);
        flat.setTeam(standing.getTeam().getId());
        flat.setPoints(standing.getPoints());
        flat.setGoalsDiff(standing.getGoalsDiff());
        flat.setForm(standing.getForm());
        flat.setStatus(standing.getStatus());
        flat.setDescription(standing.getDescription());

        return flat;
    }


}
