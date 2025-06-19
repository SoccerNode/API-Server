package com.SoccerNode.FixturesEvents.Controller;

import com.SoccerNode.FixturesEvents.Datas.FixtureEventRepository;
import com.SoccerNode.FixturesEvents.Datas.FixtureEventResponseDTO;
import com.SoccerNode.FixturesEvents.Datas.FixtureEventResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fixtures/events")
public class GetFixturesEvents {

    private final WebClient client;
    private final FixtureEventRepository fixtureEventRepository;

    @Autowired
    public GetFixturesEvents(WebClient client, FixtureEventRepository fixtureEventRepository) {
        this.client = client;
        this.fixtureEventRepository = fixtureEventRepository;
    }

    @PostMapping()
    public String getFixturesEvents(@RequestParam("fixture") int fixture) {
        Mono<FixtureEventResponseDTO> mono = getResponse(fixture);
        Flux<FixtureEventResponseDTO.FlattenedFixtureEvent> statistics = getFlattenedViaDTO(mono, fixture);

        statistics
                .map(fixtureEventRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<FixtureEventResponseDTO> getResponse(int fixture) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/statistics")
                        .queryParam("fixture", fixture)
                        .build())
                .retrieve()
                .bodyToMono(FixtureEventResponseDTO.class);
    }

    public Flux<FixtureEventResponseDTO.FlattenedFixtureEvent> getFlattenedViaDTO(Mono<FixtureEventResponseDTO> mono, int fixture) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedFixtureEvent(entry, fixture));
    }

    private Flux<FixtureEventResponseDTO.FlattenedFixtureEvent> toFlattenedFixtureEvent(FixtureEventResponseDTO.FixtureEventEntry entry, int fixture) {
        FixtureEventResponseDTO.FlattenedFixtureEvent event = new FixtureEventResponseDTO.FlattenedFixtureEvent();
        event.setFixture(fixture);
        event.setTime(entry.getTime().getElapsed());
        event.setTeam(entry.getTeam().getId());
        event.setPlayer(entry.getPlayer().getId());
        event.setAssist(entry.getAssist().getId());
        event.setType(entry.getType());
        event.setDetail(entry.getDetail());
        event.setComments(entry.getComments());

        return Flux.just(event); // ✅ 단일 객체를 Flux 로 감쌈
    }

}
