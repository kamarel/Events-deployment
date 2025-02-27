package com.myKcc.Event_Service.Service;

import com.myKcc.Event_Service.Dto.ApiResponseDto;
import com.myKcc.Event_Service.Dto.MembersDto;
import com.myKcc.Event_Service.Entity.Event;
import com.myKcc.Event_Service.Repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImp implements EventService{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private EmailService emailService;


    @Override
    public List<Event> getEventByTitle(String title) {
        return eventRepository.findEventByTitle(title);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }



    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id).get();
    }



    @Override
    public Event updateEvent(Long id, Event eventDetails) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(""));

        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setEventDate(eventDetails.getEventDate());
        event.setEventTime(eventDetails.getEventTime());
        event.setThePersonConcerned(eventDetails.getThePersonConcerned());
        event.setEventLocation(eventDetails.getEventLocation());
        event.setMessage(eventDetails.getMessage());



        Event updatedEvent = eventRepository.save(event);

        // Fetch all members from the external service
        ApiResponseDto apiResponseDto = getAllMembers();
        List<MembersDto> membersDtoList = apiResponseDto.getMembersDtoList();

        // Collect all member emails
        List<String> emails = membersDtoList.stream()
                .map(MembersDto::getEmail)
                .collect(Collectors.toList());


        emailService.eventNotification(emails, updatedEvent.getMessage() + " " + "Type" + " " + updatedEvent.getTitle() + " " + "because of" + event.getDescription());

        return updatedEvent;
    }

    @Override
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public List<Event> searchEvent(String query) {
        return eventRepository.searchEvent(query);
    }

    @Override
    public void deleteAllEvent() {
        eventRepository.deleteAll();
    }



    @Override
    public Event createEvent(Event event) {
        // Save the event with the provided message
        Event savedEvent = eventRepository.save(event);

        // Fetch all members from the external service
        ApiResponseDto apiResponseDto = getAllMembers();
        List<MembersDto> membersDtoList = apiResponseDto.getMembersDtoList();

        // Collect all member emails
        List<String> emails = membersDtoList.stream()
                .map(MembersDto::getEmail)
                .collect(Collectors.toList());


        emailService.eventNotification(
                emails,
                String.format("%s : %s that will take place at %s at %s for: %s for: %s",
                        savedEvent.getMessage(),
                        savedEvent.getTitle(),
                        savedEvent.getEventLocation(),
                        savedEvent.getEventTime(),
                        savedEvent.getThePersonConcerned(),
                        savedEvent.getDescription()

                )
        );


        return savedEvent;
    }

    @Override
    public ApiResponseDto getAllMembers() {

        Mono<List<MembersDto>> listMono = webClient.get()
                .uri("https://strong-alignment-production.up.railway.app/api/v1/members")
                .retrieve()
                .bodyToFlux(MembersDto.class)
                .collectList();


        List<MembersDto>membersDtoList = listMono.block();

        ApiResponseDto apiResponseDto = new ApiResponseDto();

        apiResponseDto.setMembersDtoList(membersDtoList);

        return apiResponseDto;
    }


}
