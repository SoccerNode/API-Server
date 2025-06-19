package com.SoccerNode.Sidelined.Controller;

import com.SoccerNode.Sidelined.Datas.CoachSidelinedRepository;
import com.SoccerNode.Sidelined.Datas.PlayerSidelinedRepository;
import com.SoccerNode.Sidelined.Datas.SidelinedResponseDTO;
import com.SoccerNode.Sidelined.Datas.SidelinedResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/trophies")
public class GetSidelined {

    private final WebClient client;
    private final PlayerSidelinedRepository playerSidelinedRepository;
    private final CoachSidelinedRepository coachSidelinedRepository;

    @Autowired
    public GetSidelined(WebClient client, PlayerSidelinedRepository playerSidelinedRepository, CoachSidelinedRepository coachSidelinedRepository) {
        this.client = client;
        this.playerSidelinedRepository = playerSidelinedRepository;
        this.coachSidelinedRepository = coachSidelinedRepository;
    }

    @PostMapping("/players")
    public String getSidelined(@RequestParam("player") int player) {
        Mono<SidelinedResponseDTO> playerMono = getPlayerResponse(player);
        Flux<FlattenedPlayerSidelined> playerVictory = getPlayerSidelinedViaDTO(player, playerMono);

        playerVictory
                .map(playerSidelinedRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/coaches")
    public String getCoachTrophies(@RequestParam("coach") int coach) {
        Mono<SidelinedResponseDTO> coachMono = getCoachResponse(coach);
        Flux<FlattenedCoachSidelined> coachVictory = getCoachSidelinedViaDTO(coach, coachMono);

        coachVictory
                .map(coachSidelinedRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<SidelinedResponseDTO> getPlayerResponse(int player) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sidelined")
                        .queryParam("player", player)
                        .build())
                .retrieve()
                .bodyToMono(SidelinedResponseDTO.class);
    }

    public Mono<SidelinedResponseDTO> getCoachResponse(int coach) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sidelined")
                        .queryParam("coach", coach)
                        .build())
                .retrieve()
                .bodyToMono(SidelinedResponseDTO.class);
    }

    public Flux<SidelinedResponseDTO.FlattenedCoachSidelined> getCoachSidelinedViaDTO(int coach, Mono<SidelinedResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> {
                            return toCoachSidelined(coach, entry);
                        }
                );
    }

    public SidelinedResponseDTO.FlattenedCoachSidelined toCoachSidelined(int coach, SidelinedResponseDTO.SidelinedEntry entry) {
        SidelinedResponseDTO.FlattenedCoachSidelined sidelined = new SidelinedResponseDTO.FlattenedCoachSidelined();

        sidelined.setCoach(coach);
        sidelined.setType(entry.getType());
        sidelined.setStart(entry.getStart());
        sidelined.setEnd(entry.getEnd());

        return sidelined;
    }

    public Flux<SidelinedResponseDTO.FlattenedPlayerSidelined> getPlayerSidelinedViaDTO(int player, Mono<SidelinedResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> {
                            return toPlayerSidelined(player, entry);
                        }
                );
    }

    public SidelinedResponseDTO.FlattenedPlayerSidelined toPlayerSidelined(int player, SidelinedResponseDTO.SidelinedEntry entry) {
        SidelinedResponseDTO.FlattenedPlayerSidelined sidelined = new SidelinedResponseDTO.FlattenedPlayerSidelined();

        sidelined.setPlayer(player);
        sidelined.setType(entry.getType());
        sidelined.setStart(entry.getStart());
        sidelined.setEnd(entry.getEnd());

        return sidelined;
    }

}
