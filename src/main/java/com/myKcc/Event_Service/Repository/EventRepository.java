package com.myKcc.Event_Service.Repository;

import com.myKcc.Event_Service.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {



    @Query("SELECT p FROM Event p WHERE " +
            "(CAST(p.id AS string) LIKE :query OR " +
            "p.title LIKE :query OR " +
            "p.description LIKE :query OR " +
            "p.thePersonConcerned LIKE :query)")
    List<Event> searchEvent(@Param("query") String query);


    List<Event>findEventByTitle(String title);




}
