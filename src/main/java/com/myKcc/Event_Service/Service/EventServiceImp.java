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

        if (membersDtoList.isEmpty()) {
            System.out.println("No members found from API.");
            return savedEvent;
        }

        // 🔹 Determine the most frequent rank in the list
        Map<String, Long> rankFrequency = membersDtoList.stream()
                .filter(member -> member.getMemberRank() != null)
                .collect(Collectors.groupingBy(MembersDto::getMemberRank, Collectors.counting()));

        // 🔹 Find the most common rank (or default to the first found)
        String mostRelevantRank = rankFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue()) // Pick the rank that appears the most
                .map(Map.Entry::getKey)
                .orElse(membersDtoList.get(0).getMemberRank()); // Default to first found rank

        // Update the event with the selected rank
        savedEvent.setThePersonConcerned(mostRelevantRank);

        // 🔹 Filter members who have the same rank
        List<String> emails = membersDtoList.stream()
                .filter(member -> mostRelevantRank.equals(member.getMemberRank())) // Match rank
                .map(MembersDto::getEmail)
                .distinct() // Ensure no duplicate emails
                .collect(Collectors.toList());

        // Debugging output
        System.out.println("Selected Rank: " + mostRelevantRank);
        System.out.println("Emails to notify: " + emails);

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
