package com.michalowicz.inzynierka.dao;

import com.michalowicz.inzynierka.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamDao extends JpaRepository<Team,Long> {
    public Team getById(Long id);
}
