package com.michalowicz.inzynierka.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.michalowicz.inzynierka.validators.OneOf;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Length(min = 5, max = 30)
    private String name;

    @Length(max = 200)
    private String description;

    @OneOf({2, 4, 8, 16, 32})
    private int teamsNeeded;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.Otwarty;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeCreated = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @JsonIgnoreProperties(value = {"ownedTournaments", "joinedTournaments", "teams", "usergroup"})
    private User owner;
    @Transient
    @JsonIgnoreProperties(value = {"joinedTournaments", "ownedTournaments", "teams", "usergroup"})
    private Set<User> participants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tournament", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade({CascadeType.SAVE_UPDATE ,CascadeType.DELETE})
    @JsonIgnoreProperties(value = {"tournament"})
    private List<Team> teams = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonIgnoreProperties(value = {"tournaments"})
    private RuleSet ruleSet;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JsonIgnoreProperties(value = {"tournament"})
    private Set<Match> matches = new LinkedHashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"tournament"})
    private Team winner;

    @Transient
    private boolean isReadyToStart;

    public Tournament() {
    }

    public Tournament(@NotBlank final String name) {
        this.name = name;
    }

    public Tournament(@NotBlank final String name, final User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(final User owner) {
        this.owner = owner;
    }

    public void addOwner(final User owner) {
        this.owner = owner;
        owner.getOwnedTournaments().add(this);
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(final TournamentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(final LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Set<User> getParticipants() {
        Set<User> participants = new HashSet<>();
        teams.stream().map(Team::getPlayers).forEach(participants::addAll);
        return participants;
    }

    @PostLoad
    public void postLoad() {
        setParticipants();
        setReadyToStart();
    }

    public void addParticipant(final User user) {
        this.participants.add(user);
        user.getJoinedTournaments().add(this);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(final List<Team> teams) {
        this.teams = teams;
    }

    public void addTeam(final Team team) {
        this.teams.add(team);
        team.setTournament(this);
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(final RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    public void addRuleSet(final RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        ruleSet.getTournaments().add(this);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public void setMatches(final Set<Match> matches) {
        this.matches = matches;
    }

    public void addMatch(final Match match) {
        this.matches.add(match);
        match.setTournament(this);
    }

    public int getTeamsNeeded() {
        return teamsNeeded;
    }

    public void setTeamsNeeded(final int teamsNeeded) {
        this.teamsNeeded = teamsNeeded;
    }

    public Team getWinner() {
        return winner;
    }

    public void setWinner(final Team winner) {
        this.winner = winner;
    }

    public boolean isReadyToStart() {
        return isReadyToStart;
    }

    private void setParticipants(){
        Set<User> participants = new HashSet<>();
        teams.stream().map(Team::getPlayers).forEach(participants::addAll);
        this.participants = participants;
    }

    private void setReadyToStart() {
        isReadyToStart = teams.size() == teamsNeeded && teams.stream().noneMatch(team -> team.getPlayers().size() < ruleSet.getTeamSize());
    }
}
