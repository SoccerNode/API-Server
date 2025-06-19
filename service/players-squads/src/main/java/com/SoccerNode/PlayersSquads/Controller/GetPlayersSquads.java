package com.SoccerNode.PlayersSquads.Controller;

import com.SoccerNode.PlayersSquads.Datas.PlayerSquadRepository;
import com.SoccerNode.PlayersSquads.Datas.PlayerSquadResponseDTO;
import com.SoccerNode.PlayersSquads.Datas.PlayerSquadResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/players/squads")
public class GetPlayersSquads {

    private final WebClient client;
    private final PlayerSquadRepository playerSquadRepository;

    @Autowired
    public GetPlayersSquads(WebClient client, PlayerSquadRepository playerSquadRepository) {
        this.client = client;
        this.playerSquadRepository = playerSquadRepository;
    }

    @PostMapping()
    public String getPlayersSquads(@RequestParam("team") int team) {
        Mono<PlayerSquadResponseDTO> mono = getResponse(team);
        Flux<FlattenedSquad> squad = getFlattenedViaDTO(mono);

        squad
                .map(playerSquadRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<PlayerSquadResponseDTO> getResponse(int team) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players/squads")
                        .queryParam("team", team)
                        .build())
                .retrieve()
                .bodyToMono(PlayerSquadResponseDTO.class);
    }

    public Flux<FlattenedSquad> getFlattenedViaDTO(Mono<PlayerSquadResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(this::toFlattenedSquad);
    }

    private Flux<FlattenedSquad> toFlattenedSquad(TeamSquadEntry entry) {
        int team = entry.getTeam().getId();

        return Flux.fromIterable(entry.getPlayers())
                .map(player -> toSquad(team, player));
    }

    private FlattenedSquad toSquad(int team, Player player) {
        FlattenedSquad squad = new FlattenedSquad();
        squad.setTeam(team);
        squad.setPlayer(player.getId());
        squad.setNumber(player.getNumber());
        squad.setPosition(player.getPosition());

        return squad;
    }

}
