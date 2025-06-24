package com.SoccerNode.Injuries.Controller;

import com.SoccerNode.Injuries.Datas.InjuryRepository;
import com.SoccerNode.Injuries.Datas.InjuryResponseDTO;
import com.SoccerNode.Injuries.Datas.InjuryResponseDTO.*;
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
@RequestMapping("/injuries")
public class GetInjuries {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final InjuryRepository injuryRepository;

    @Autowired
    public GetInjuries(WebClient client, MongoTemplate mongoTemplate, InjuryRepository injuryRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
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

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateInjuries();
        return "Cleaned up";
    }

    public void deduplicateInjuries() {
        List<FlattenedInjury> all = mongoTemplate.findAll(FlattenedInjury.class);
        Map<String, List<FlattenedInjury>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getFixture() + "|" + l.getPlayer() + "|" + l.getType() + " | " + l.getReason()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedInjury> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedInjury l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedInjury::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedInjury.class);
        }
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
