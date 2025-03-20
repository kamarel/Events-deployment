package com.myKcc.Event_Service.Service;

import com.myKcc.Event_Service.Dto.ApiResponseDto;
import com.myKcc.Event_Service.Entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<Event>getEventByTitle(String title);
    List<Event>getAllEvents();
    Event getEventById(Long id);
    Event createEvent(Event event, String token);
    Event updateEvent(Long id, Event eventDetails, String token);
    void deleteEventById(Long id);
    List<Event>searchEvent(String query);
    void deleteAllEvent();
    ApiResponseDto getAllMembers(String token);

}
