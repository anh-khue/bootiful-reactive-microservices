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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

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

    @Bean
    @RefreshScope
    RouterFunction routes(@Value("${message}") String message,
                          ReservationRepository repository) {
        return RouterFunctions
                .route(GET("/message"),
                        request -> ok().body(Mono.just(message), String.class))
                .andRoute(GET("/reservations"),
                        request -> ok().body(repository.findAll(), Reservation.class))
                .andRoute(GET("/reservations/{id}"),
                        request -> ok().body(repository.findById(request.pathVariable("id")), Reservation.class));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}


@Component
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