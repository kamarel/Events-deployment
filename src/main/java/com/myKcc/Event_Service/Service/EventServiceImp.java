package com.myKcc.Event_Service.Service;

import com.myKcc.Event_Service.Dto.ApiResponseDto;
import com.myKcc.Event_Service.Dto.MembersDto;
import com.myKcc.Event_Service.Entity.Event;
import com.myKcc.Event_Service.Repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EventServiceImp implements EventService{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private EmailService emailService;


 /*   public Event saveEvent(Event event){
        return eventRepository.save(event);
    }*/

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
    public Event updateEvent(Long id, Event eventDetails, String token) {

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
        ApiResponseDto apiResponseDto = getAllMembers(token);
        List<MembersDto> membersDtoList = apiResponseDto.getMembersDtoList();

        // Collect all member emails
        List<String> emails = membersDtoList.stream()
                .map(MembersDto::getEmail)
                .collect(Collectors.toList());


        emailService.eventNotifications(emails, updatedEvent.getMessage() + " " + "Type" + " " + updatedEvent.getTitle() + " " + "because of" + event.getDescription());

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
    public Event createEvent(Event event, String token) {
        // Save the event with the provided message
        Event savedEvent = eventRepository.save(event);

        // Fetch all members from the external service
        ApiResponseDto apiResponseDto = getAllMembers(token);
        List<MembersDto> membersDtoList = apiResponseDto.getMembersDtoList();

        // Determine the most relevant rank (you can change this logic as needed)
        String mostRelevantRank = membersDtoList.stream()
                .map(MembersDto::getMemberRank)
                .filter(Objects::nonNull)
                .findFirst() // Picks the first available rank
                .orElse("Unknown"); // Default value if no rank is found

        // Update the event with the determined rank
        savedEvent.setThePersonConcerned(mostRelevantRank);

        // Filter members by the determined rank
        List<String> emails = membersDtoList.stream()
                .filter(member -> mostRelevantRank.equals(member.getMemberRank())) // Only members with this rank
                .map(MembersDto::getEmail)
                .collect(Collectors.toList());

        // Debug: Check which emails will be sent
        System.out.println("Sending email to members with rank: " + mostRelevantRank);
        System.out.println("Emails: " + emails);

        if (!emails.isEmpty()) {
            try {
                emailService.eventNotification(
                        emails,
                        String.format("%s : %s that will take place at %s at %s for: %s for: %s",
                                savedEvent.getMessage(),
                                savedEvent.getTitle(),
                                savedEvent.getEventLocation(),
                                savedEvent.getEventTime(),
                                mostRelevantRank, // Automatically selected rank
                                savedEvent.getDescription()
                        )
                );
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No members found with rank: " + mostRelevantRank);
        }

        return savedEvent;
    }









    @Override
    public ApiResponseDto getAllMembers(String token) {
        Mono<List<MembersDto>> listMono = webClient.get()
                .uri("https://distinguished-education-production.up.railway.app/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToFlux(MembersDto.class)
                .collectList();

        List<MembersDto> membersDtoList = listMono.block();

        ApiResponseDto apiResponseDto = new ApiResponseDto();
        apiResponseDto.setMembersDtoList(membersDtoList);
        return apiResponseDto;
    }


}
