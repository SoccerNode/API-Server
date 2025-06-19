package com.SoccerNode.FixturesLineups.Controller;

import com.SoccerNode.FixturesLineups.Datas.FixtureLineupRepository;
import com.SoccerNode.FixturesLineups.Datas.FixtureLineupResponseDTO;
import com.SoccerNode.FixturesLineups.Datas.FixtureLineupResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fixtures/lineups")
public class GetFixturesLineups {

    private final WebClient client;
    private final FixtureLineupRepository fixtureLineupRepository;

    @Autowired
    public GetFixturesLineups(WebClient client, FixtureLineupRepository fixtureLineupRepository) {
        this.client = client;
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
