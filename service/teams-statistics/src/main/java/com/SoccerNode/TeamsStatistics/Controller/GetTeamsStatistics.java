package com.SoccerNode.TeamsStatistics.Controller;

import com.SoccerNode.TeamsStatistics.Datas.TeamStatisticRepository;
import com.SoccerNode.TeamsStatistics.Datas.TeamStatisticResponseDTO;
import com.SoccerNode.TeamsStatistics.Datas.TeamStatisticResponseDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/teams/statistics")
public class GetTeamsStatistics {

    private final WebClient client;
    private final MongoTemplate mongoTemplate;
    private final TeamStatisticRepository teamStatisticRepository;

    @Autowired
    public GetTeamsStatistics(WebClient client, MongoTemplate mongoTemplate, TeamStatisticRepository teamStatisticRepository) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.teamStatisticRepository = teamStatisticRepository;
    }

    @PostMapping()
    public String getTeamsStatistics(@RequestParam("league") int league, @RequestParam("team") int team, @RequestParam("season") int season) {
        Mono<TeamStatisticResponseDTO> mono = getResponse(league, team, season);
        Flux<FlattenedTeamStatistic> teamStatistic = getFlattenedViaDTO(mono);

        teamStatistic
                .map(teamStatisticRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved))
                .doOnError(e -> System.err.println("Save error: " + e.getMessage()))
                .subscribe();

        return "Request completed";
    }

    public Mono<TeamStatisticResponseDTO> getResponse(int league, int team, int season) {
        return this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams/statistics")
                        .queryParam("league", league)
                        .queryParam("team", team)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(TeamStatisticResponseDTO.class);
    }

    public Flux<FlattenedTeamStatistic> getFlattenedViaDTO(Mono<TeamStatisticResponseDTO> mono) {
        return mono
                .map(TeamStatisticResponseDTO::getResponse)
                .map(this::toFlattenedTeamStatistic)
                .flux();
    }

    private FlattenedTeamStatistic toFlattenedTeamStatistic(TeamStatisticResponseDTO.TeamStatisticEntry entry) {
        Fixtures f = entry.getFixtures();

        FlattenedTeamStatistic result = new FlattenedTeamStatistic();
        result.setLeague(entry.getLeague().getId());
        result.setSeason(entry.getLeague().getSeason());
        result.setTeam(entry.getTeam().getId());
        result.setForm(entry.getForm());

        result.setHomePlayed(f.getPlayed().getHome());
        result.setHomeWin(f.getWins().getHome());
        result.setHomeGoal(entry.getGoals().get_for().getTotal().getHome());

        result.setAwayPlayed(f.getPlayed().getAway());
        result.setAwayWin(f.getWins().getAway());
        result.setAwayGoal(entry.getGoals().get_for().getTotal().getAway());

        result.setPenaltyCount(entry.getPenalty().getTotal());
        result.setPenaltyScored(entry.getPenalty().getScored().getTotal());

        result.setCardsYellow60(sumCards(entry.getCards().getYellow(), 0, 60));
        result.setCardsYellow120(sumCards(entry.getCards().getYellow(), 61, 120));
        result.setCardsRed60(sumCards(entry.getCards().getRed(), 0, 60));
        result.setCardsRed120(sumCards(entry.getCards().getRed(), 61, 120));

        return result;
    }

    private int sumCards(Object cardObj, int fromMinute, int toMinute) {
        int sum = 0;
        try {
            for (int i = fromMinute; i <= toMinute; i += 15) {
                String method = "get" + getTimeFieldName(i);
                Card card = (Card) cardObj.getClass().getMethod(method).invoke(cardObj);
                if (card.getTotal() != null) sum += card.getTotal();
            }
        } catch (Exception ignored) {}
        return sum;
    }

    private String getTimeFieldName(int minute) {
        switch (minute) {
            case 0: return "Time15";
            case 16: return "Time30";
            case 31: return "Time45";
            case 46: return "Time60";
            case 61: return "Time75";
            case 76: return "Time90";
            case 91: return "Time105";
            case 106: return "Time120";
            default: throw new IllegalArgumentException("Invalid minute: " + minute);
        }
    }

}
