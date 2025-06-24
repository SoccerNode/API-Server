package com.SoccerNode.FixturesEvents.Controller;

import com.SoccerNode.FixturesEvents.Datas.FixtureEventRepository;
import com.SoccerNode.FixturesEvents.Datas.FixtureEventResponseDTO;
import com.SoccerNode.FixturesEvents.Datas.FixtureEventResponseDTO.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fixtures/events")
public class GetFixturesEvents {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final FixtureEventRepository fixtureEventRepository;

    @Autowired
    public GetFixturesEvents(WebClient client, MongoTemplate mongoTemplate, FixtureEventRepository fixtureEventRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
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

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateFixturesEvents();
        return "Cleaned up";
    }

    public void deduplicateFixturesEvents() {
        List<FlattenedFixtureEvent> all = mongoTemplate.findAll(FlattenedFixtureEvent.class);
        Map<String, List<FlattenedFixtureEvent>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture() + "|" + l.getTime() + "|" + l.getTeam() + "|" + l.getPlayer()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedFixtureEvent> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedFixtureEvent l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedFixtureEvent::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedFixtureEvent.class);
        }
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
