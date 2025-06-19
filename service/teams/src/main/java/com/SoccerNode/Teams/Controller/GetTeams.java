package com.SoccerNode.Teams.Controller;

import com.SoccerNode.Teams.Datas.TeamRepository;
import com.SoccerNode.Teams.Datas.TeamResponseDTO;
import com.SoccerNode.Teams.Datas.TeamResponseDTO.*;
import com.SoccerNode.Teams.Datas.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/teams")
public class GetTeams {

    private final WebClient client;
    private final TeamRepository teamRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public GetTeams(WebClient client, TeamRepository teamRepository, VenueRepository venueRepository) {
        this.client = client;
        this.teamRepository = teamRepository;
        this.venueRepository = venueRepository;
    }

    @PostMapping()
    public String getTeams(@RequestParam("league") int league, @RequestParam("season") int season) {
        Mono<TeamResponseDTO> response = getResponse(league, season);
        Flux<Team> teams = getTeamsViaDTO(response);
        Flux<Venue> venues = getVenuesViaDTO(response);

        teams
                .map(teamRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        venues
                .map(venueRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<TeamResponseDTO> getResponse(int league, int season) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("league", league)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(TeamResponseDTO.class);
    }


    public Flux<Venue> getVenuesViaDTO(Mono<TeamResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(TeamEntry::getVenue);
    }

    public Flux<Team> getTeamsViaDTO(Mono<TeamResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(TeamEntry::getTeam);
    }

}
