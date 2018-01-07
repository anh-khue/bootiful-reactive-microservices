package com.khuetla.reactive.reservationservice;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReservationServiceApplication {

    @Bean
    ApplicationRunner dataInit(ReservationRepository reservationRepository) {
        return args ->
                reservationRepository
                        .deleteAll()
                        .thenMany(
                                Flux.just("Java", "Servlet", "Spring",
                                        "Microservices", "Cloud Native", "Serverless")
                                        .map(Reservation::new)
                                        .flatMap(reservationRepository::save))
                        .thenMany(reservationRepository.findAll())
                        .subscribe(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}


@RestController
@RefreshScope
class MessageRestController {

    private final String message;

    MessageRestController(@Value("${message}") String message) {
        this.message = message;
    }

    @GetMapping("/message")
    Publisher<String> getMessage() {
        return Mono.just(this.message);
    }
}


@RestController
@RequestMapping("/reservations")
class ReservationApiRestController {

    private final ReservationRepository reservationRepository;

    ReservationApiRestController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    Flux<Reservation> getReservations() {
        return this.reservationRepository.findAll();
    }

    @GetMapping("{id}")
    Mono<Reservation> getReservationIdBy(@PathVariable("id") String id) {
        return this.reservationRepository.findById(id);
    }
}


interface ReservationRepository extends ReactiveMongoRepository<Reservation, String> {

}


@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Reservation {

    @Id
    private String id;
    @NonNull
    private String name;
}