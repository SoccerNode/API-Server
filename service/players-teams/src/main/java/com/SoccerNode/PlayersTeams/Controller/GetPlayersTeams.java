package com.SoccerNode.PlayersTeams.Controller;

import com.SoccerNode.PlayersTeams.Datas.PlayerTeamRepository;
import com.SoccerNode.PlayersTeams.Datas.PlayerTeamResponseDTO;
import com.SoccerNode.PlayersTeams.Datas.PlayerTeamResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/players/teams")
public class GetPlayersTeams {

    private final WebClient client;
    private final PlayerTeamRepository playerTeamRepository;

    @Autowired
    public GetPlayersTeams(WebClient client, PlayerTeamRepository playerTeamRepository) {
        this.client = client;
        this.playerTeamRepository = playerTeamRepository;
    }

    @PostMapping()
    public String getPlayersTeams(@RequestParam("player") int player) {
        Mono<PlayerTeamResponseDTO> mono = getResponse(player);
        Flux<FlattenedTeam> teams = getFlattenedTeamViaDTO(mono, player);

        teams
                .map(playerTeamRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<PlayerTeamResponseDTO> getResponse(int player) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players/teams")
                        .queryParam("player", player)
                        .build())
                .retrieve()
                .bodyToMono(PlayerTeamResponseDTO.class);
    }

    public Flux<FlattenedTeam> getFlattenedTeamViaDTO(Mono<PlayerTeamResponseDTO> mono, int player) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> {
                    return toTeam(player, entry);
                });
    }

    private FlattenedTeam toTeam(int player,  TeamsEntry entry) {
        FlattenedTeam team = new FlattenedTeam();
        team.setPlayer(player);
        team.setTeam(entry.getTeam().getId());

        StringBuilder season = new StringBuilder();
        try {
            for (String s : entry.getSeasons()) {
                season.append(s);
                season.append(", ");
            }
            if (season.length() > 4) {
                season.delete(season.length() - 2, season.length());
            }
        } catch (Exception e) {

        }
        team.setSeasons(season.toString());

        return team;
    }

}
