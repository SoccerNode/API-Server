package com.SoccerNode.Coaches.Controller;

import com.SoccerNode.Coaches.Datas.CareerRepository;
import com.SoccerNode.Coaches.Datas.CoachRepository;
import com.SoccerNode.Coaches.Datas.CoachResponseDTO;
import com.SoccerNode.Coaches.Datas.CoachResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/coaches")
public class GetCoaches {

    private final WebClient client;
    private final CoachRepository coachRepository;
    private final CareerRepository careerRepository;

    @Autowired
    public GetCoaches(WebClient client, CoachRepository coachRepository, CareerRepository careerRepository) {
        this.client = client;
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


    public Mono<CoachResponseDTO> getResponse(int team) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/coachs")
                        .queryParam("team", team)
                        .build())
                .retrieve()
                .bodyToMono(CoachResponseDTO.class);
    }

    public Flux<CoachResponseDTO.FlattenedCareer> getFlattenedCareerViaDTO(Mono<CoachResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .flatMap(entry -> toFlattenedCareerViaDTO(entry));
    }

    // Career

    private Flux<CoachResponseDTO.FlattenedCareer> toFlattenedCareerViaDTO(CoachResponseDTO.CoachEntry entry) {
        int coach = entry.getId();

        return Flux.fromIterable(entry.getCareer())
                .map(career -> toCareer(coach, career));
    }

    private CoachResponseDTO.FlattenedCareer toCareer(int coach, CoachResponseDTO.Career career) {
        CoachResponseDTO.FlattenedCareer flat = new CoachResponseDTO.FlattenedCareer();
        flat.setCoach(coach);
        flat.setTeam(career.getTeam().getId());
        flat.setStart(career.getStart());
        flat.setEnd(career.getEnd());

        return flat;
    }

    // Coach

    public Flux<CoachResponseDTO.FlattenedCoach> getFlattenedCoachViaDTO(Mono<CoachResponseDTO> mono) {
        return mono
                .flatMapMany(dto -> Flux.fromIterable(dto.getResponse()))
                .map(this::toCoach);
    }

    private CoachResponseDTO.FlattenedCoach toCoach(CoachResponseDTO.CoachEntry entry) {
        CoachResponseDTO.FlattenedCoach coach = new CoachResponseDTO.FlattenedCoach();
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
