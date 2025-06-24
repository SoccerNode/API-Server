package com.SoccerNode.Coaches.Controller;

import com.SoccerNode.Coaches.Datas.CareerRepository;
import com.SoccerNode.Coaches.Datas.CoachRepository;
import com.SoccerNode.Coaches.Datas.CoachResponseDTO;
import com.SoccerNode.Coaches.Datas.CoachResponseDTO.*;
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
@RequestMapping("/coaches")
public class GetCoaches {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final CoachRepository coachRepository;
    private final CareerRepository careerRepository;

    @Autowired
    public GetCoaches(WebClient client, MongoTemplate mongoTemplate, CoachRepository coachRepository, CareerRepository careerRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.coachRepository = coachRepository;
        this.careerRepository = careerRepository;
    }

    @PostMapping()
    public String getCoaches(@RequestParam("team") int team) {
        Mono<CoachResponseDTO> mono = getResponse(team);
        Flux<FlattenedCoach> coachs = getFlattenedCoachViaDTO(mono);
        Flux<FlattenedCareer> careers = getFlattenedCareerViaDTO(mono);

        coachs
                .map(coachRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        careers
                .map(careerRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    @PostMapping("/cleansing")
    public String removeDuplicated() {
        deduplicateCoaches();
        deduplicateCareers();
        return "Cleaned up";
    }

    public void deduplicateCoaches() {
        List<FlattenedCoach> all = mongoTemplate.findAll(FlattenedCoach.class);
        Map<String, List<FlattenedCoach>> grouped = all.stream()
                .collect(Collectors.groupingBy(l ->
                        l.getId() + "|" + l.getName()
                ));

        List<ObjectId> toDelete = new ArrayList<>();
        for (List<FlattenedCoach> group : grouped.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparing((FlattenedCoach l) -> l.get_id().getTimestamp()).reversed());
            toDelete.addAll(
                    group.subList(1, group.size())
                            .stream()
                            .map(FlattenedCoach::get_id)
                            .toList()
            );
        }

        if (!toDelete.isEmpty()) {
            Query deleteQuery = new Query(Criteria.where("_id").in(toDelete));
            mongoTemplate.remove(deleteQuery, FlattenedCoach.class);
        }
    }

    public void deduplicateCareers() {
        // RM ALL
    }

    public Mono<CoachResponseDTO> getResponse(int team) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/coachs")
                        .queryParam("team", team)
                        .build())
                .retrieve()
                .bodyToMono(CoachResponseDTO.class);
    }

    public Flux<FlattenedCareer> getFlattenedCareerViaDTO(Mono<CoachResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedCareerViaDTO(entry));
    }

    // Career
    private Flux<FlattenedCareer> toFlattenedCareerViaDTO(CoachEntry entry) {
        int coach = entry.getId();

        return Flux.fromIterable(entry.getCareer())
                .map(career -> toCareer(coach, career));
    }

    private FlattenedCareer toCareer(int coach, Career career) {
        FlattenedCareer flat = new FlattenedCareer();
        flat.setCoach(coach);
        flat.setTeam(career.getTeam().getId());
        flat.setStart(career.getStart());
        flat.setEnd(career.getEnd());

        return flat;
    }

    // Coach
    public Flux<FlattenedCoach> getFlattenedCoachViaDTO(Mono<CoachResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(this::toCoach);
    }

    private FlattenedCoach toCoach(CoachEntry entry) {
        FlattenedCoach coach = new FlattenedCoach();
        coach.setId(entry.getId());
        coach.setName(entry.getName());
        coach.setFirstname(entry.getFirstname());
        coach.setLastname(entry.getLastname());
        coach.setBirth(entry.getBirth().getDate());
        coach.setNationality(entry.getNationality());
        coach.setHeight(entry.getHeight());
        coach.setWeight(entry.getWeight());
        coach.setPhoto(entry.getPhoto());

        return coach;
    }

}
