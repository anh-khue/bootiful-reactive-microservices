package com.khuetla.reactive.reservationservice;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}


@Component
class DataInitializer implements ApplicationRunner {

    private final ReservationRepository reservationRepository;

    DataInitializer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
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