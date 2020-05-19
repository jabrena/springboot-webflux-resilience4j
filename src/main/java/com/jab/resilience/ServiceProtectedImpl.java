package com.jab.resilience;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.jab.resilience.Constants.CIRCUIT_BREAKER_1;
import static com.jab.resilience.Constants.FALLBACK_GOD_RESPONSE;

@Slf4j
@Service
public class ServiceProtectedImpl implements ServiceProtected {

    private CircuitBreakerFactory circuitBreakerFactory;

    public ServiceProtectedImpl(CircuitBreakerFactory circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public String retrieve(String url) {

        Function<String, Flux<String>> serializeFlux = param -> Try.of(() -> {
            if(param.length() == 0) return Flux.just("");
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> deserializedData = objectMapper.readValue(param, new TypeReference<List<String>>() {});
            return Mono.just(deserializedData).flatMapMany(Flux::fromIterable);
        }).getOrElseThrow(ex -> {
            LOGGER.error("Bad Serialization process", ex);
            throw new RuntimeException(ex);
        });

        Function<String, List<String>> serializeList = param -> Try.of(() -> {
            if(param.length() == 0) return List.of("");
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> deserializedData = objectMapper.readValue(param, new TypeReference<List<String>>() {});
            return deserializedData;
        }).getOrElseThrow(ex -> {
            LOGGER.error("Bad Serialization process", ex);
            throw new RuntimeException(ex);
        });

        Function<String, List<String>> externalCall = param -> {

            WebClient client = WebClient
                    .builder()
                    //.baseUrl(param)
                    .build();

            var response = client
                    .method(HttpMethod.GET)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    //.doOnNext(x -> LOGGER.info(x.toString()))
                    .doOnError(ex -> LOGGER.error(ex.getLocalizedMessage(), ex))
                    .block();

            var list = serializeList.apply(response);
            list.stream().forEach(LOGGER::info);

            return list;
        };

        Function<String, List<String>>  circuitBreakerRetrieve = param -> {
             CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_1);

             return circuitBreaker.run(() -> externalCall.apply(param),
                throwable -> List.of(FALLBACK_GOD_RESPONSE));
        };

        Function<List<String>, String> getFirst = list -> list.stream()
            .peek(LOGGER::info)
            .findFirst()
            .orElse(FALLBACK_GOD_RESPONSE);

        return circuitBreakerRetrieve.andThen(getFirst).apply(url);
    }

}
