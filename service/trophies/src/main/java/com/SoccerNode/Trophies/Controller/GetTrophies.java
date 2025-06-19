package com.SoccerNode.Trophies.Controller;

import com.SoccerNode.Trophies.Datas.CoachTrophyRepository;
import com.SoccerNode.Trophies.Datas.PlayerTrophyRepository;
import com.SoccerNode.Trophies.Datas.TrophyResponseDTO;
import com.SoccerNode.Trophies.Datas.TrophyResponseDTO.*;
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
public class GetTrophies {

    private final WebClient client;
    private final PlayerTrophyRepository playerTrophyRepository;
    private final CoachTrophyRepository coachTrophyRepository;

    @Autowired
    public GetTrophies(WebClient client, PlayerTrophyRepository playerTrophyRepository, CoachTrophyRepository coachTrophyRepository) {
        this.client = client;
        this.playerTrophyRepository = playerTrophyRepository;
        this.coachTrophyRepository = coachTrophyRepository;
    }

    @PostMapping("/players")
    public String getPlayersTrophies(@RequestParam("player") int player) {
        Mono<TrophyResponseDTO> playerMono = getPlayerResponse(player);
        Flux<TrophyResponseDTO.FlattenedPlayerTrophy> playerVictory = getPlayerTrophiesViaDTO(player, playerMono);

        playerVictory
                .map(playerTrophyRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/coaches")
    public String getCoachesTrophies(@RequestParam("coach") int coach) {
        Mono<TrophyResponseDTO> coachMono = getCoachResponse(coach);
        Flux<FlattenedCoachTrophy> coachVictory = getCoachTrophiesViaDTO(coach, coachMono);

        coachVictory
                .map(coachTrophyRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<TrophyResponseDTO> getPlayerResponse(int player) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trophies")
                        .queryParam("player", player)
                        .build())
                .retrieve()
                .bodyToMono(TrophyResponseDTO.class);
    }

    public Mono<TrophyResponseDTO> getCoachResponse(int coach) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trophies")
                        .queryParam("coach", coach)
                        .build())
                .retrieve()
                .bodyToMono(TrophyResponseDTO.class);
    }

    public Flux<FlattenedCoachTrophy> getCoachTrophiesViaDTO(int coach, Mono<TrophyResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> {
                            return toCoachTrophy(coach, entry);
                        }
                );
    }

    public FlattenedCoachTrophy toCoachTrophy(int coach, TrophyEntry entry) {
        FlattenedCoachTrophy trophy = new FlattenedCoachTrophy();

        trophy.setCoach(coach);
        trophy.setLeague(entry.getLeague());
        trophy.setCountry(entry.getCountry());
        trophy.setSeason(entry.getSeason());
        trophy.setPlace(entry.getPlace());

        return trophy;
    }

    public Flux<FlattenedPlayerTrophy> getPlayerTrophiesViaDTO(int player, Mono<TrophyResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> {
                            return toPlayerTrophy(player, entry);
                        }
                );
    }

    public FlattenedPlayerTrophy toPlayerTrophy(int player, TrophyEntry entry) {
        FlattenedPlayerTrophy trophy = new FlattenedPlayerTrophy();

        trophy.setPlayer(player);
        trophy.setLeague(entry.getLeague());
        trophy.setCountry(entry.getCountry());
        trophy.setSeason(entry.getSeason());
        trophy.setPlace(entry.getPlace());

        return trophy;
    }

}
