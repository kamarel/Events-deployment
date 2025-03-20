package com.myKcc.Event_Service.Controller;

import com.myKcc.Event_Service.Entity.Event;

import com.myKcc.Event_Service.Service.EventServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventServiceImp eventService;



    // Create an event and send email notifications
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event,
                                             @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        Event createdEvent = eventService.createEvent(event, token);
        return ResponseEntity.ok(createdEvent);
    }

    // Get all events
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    // Get an event by ID
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    // Update an event by ID
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails,
                                             @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        Event updatedEvent = eventService.updateEvent(id, eventDetails, token);
        return ResponseEntity.ok(updatedEvent);
    }

    // Delete an event by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable Long id) {
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }

    // Search events by query (e.g., title or description)
    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String query) {
        List<Event> events = eventService.searchEvent(query);
        return ResponseEntity.ok(events);
    }

    // Delete all events
    @DeleteMapping
    public ResponseEntity<Void> deleteAllEvents() {
        eventService.deleteAllEvent();
        return ResponseEntity.noContent().build();
    }
}
