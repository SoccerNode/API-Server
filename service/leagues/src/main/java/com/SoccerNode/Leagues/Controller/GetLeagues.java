package com.SoccerNode.Leagues.Controller;

import com.SoccerNode.Leagues.Datas.LeagueRepository;
import com.SoccerNode.Leagues.Datas.LeagueResponseDTO;
import com.SoccerNode.Leagues.Datas.LeagueResponseDTO.*;
import com.SoccerNode.Leagues.Datas.SeasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/leagues")
public class GetLeagues {

    private final WebClient client;
    private final LeagueRepository leagueRepository;
    private final SeasonRepository seasonRepository;

    @Autowired
    public GetLeagues(WebClient client, LeagueRepository leagueRepository, SeasonRepository seasonRepository) {
        this.client = client;
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
