package com.SoccerNode.Injuries.Controller;

import com.SoccerNode.Injuries.Datas.InjuryRepository;
import com.SoccerNode.Injuries.Datas.InjuryResponseDTO;
import com.SoccerNode.Injuries.Datas.InjuryResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/injuries")
public class GetInjuries {

    private final WebClient client;
    private final InjuryRepository injuryRepository;

    @Autowired
    public GetInjuries(WebClient client, InjuryRepository injuryRepository) {
        this.client = client;
        this.injuryRepository = injuryRepository;
    }

    @PostMapping()
    public String getInjuries(@RequestParam("fixture") int fixture) {
        Mono<InjuryResponseDTO> mono = getResponse(fixture);
        Flux<FlattenedInjury> injuries = getInjuryViaDTO(fixture, mono);

        injuries
                .map(injuryRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<InjuryResponseDTO> getResponse(int fixture) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/injuries")
                        .queryParam("fixture", fixture)
                        .build())
                .retrieve()
                .bodyToMono(InjuryResponseDTO.class);
    }

    public Flux<InjuryResponseDTO.FlattenedInjury> getInjuryViaDTO(int fixture, Mono<InjuryResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(entry -> toFlattenedInjury(fixture, entry));
    }


    private InjuryResponseDTO.FlattenedInjury toFlattenedInjury(int fixture, InjuryResponseDTO.InjuryEntry entry) {
        InjuryResponseDTO.Player player = entry.getPlayer();

        InjuryResponseDTO.FlattenedInjury flat = new InjuryResponseDTO.FlattenedInjury();
        flat.setFixture(fixture);
        flat.setPlayer(player.getId());
        flat.setType(player.getType());
        flat.setReason(player.getReason());

        return flat;
    }

}
