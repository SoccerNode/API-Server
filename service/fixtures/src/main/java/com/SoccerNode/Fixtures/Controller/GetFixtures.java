package com.SoccerNode.Fixtures.Controller;

import com.SoccerNode.Fixtures.Datas.FixtureRepository;
import com.SoccerNode.Fixtures.Datas.FixtureResponseDTO;
import com.SoccerNode.Fixtures.Datas.FixtureResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fixtures")
public class GetFixtures {

    private final WebClient client;
    private final FixtureRepository fixtureRepository;

    @Autowired
    public GetFixtures(WebClient client, FixtureRepository fixtureRepository) {
        this.client = client;
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
