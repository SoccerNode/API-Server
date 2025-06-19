package com.SoccerNode.FixturesStatistics.Controller;

import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticRepository;
import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticResponseDTO;
import com.SoccerNode.FixturesStatistics.Datas.FixtureStatisticResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fixtures/statistics")
public class GetFixtureStatistic {

    private final WebClient client;
    private final FixtureStatisticRepository fixtureStatisticRepository;

    @Autowired
    public GetFixtureStatistic(WebClient client, FixtureStatisticRepository fixtureStatisticRepository) {
        this.client = client;
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
