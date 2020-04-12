package lol.maki.azuredns.home;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@RestController
@Profile("default")
public class DevProxyController {
    private final WebClient webClient;

    public DevProxyController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @GetMapping(path = "/index.html")
    public Mono<ResponseEntity<ByteBuffer>> html() {
        return this.webClient.get().uri("http://localhost:8081/index.html")
                .exchange()
                .flatMap(res -> res.toEntity(ByteBuffer.class));
    }

    @GetMapping(path = "/bundle.js")
    public Mono<ResponseEntity<ByteBuffer>> js() {
        return this.webClient.get().uri("http://localhost:8081/bundle.js")
                .exchange()
                .flatMap(res -> res.toEntity(ByteBuffer.class));
    }
}
