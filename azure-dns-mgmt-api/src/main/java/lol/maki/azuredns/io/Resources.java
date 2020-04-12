package lol.maki.azuredns.io;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Resources {

    public static Mono<byte[]> copyToByteArray(Resource resource) {
        return Mono.fromCallable(() -> {
            try (final InputStream stream = resource.getInputStream()) {
                return StreamUtils.copyToByteArray(stream);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<String> copyToString(Resource resource) {
        return Mono.fromCallable(() -> {
            try (final InputStream stream = resource.getInputStream()) {
                return StreamUtils.copyToString(stream, UTF_8);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<Void> copy(String s, WritableResource resource) {
        return Mono.fromCallable(() -> {
            try (final OutputStream stream = resource.getOutputStream()) {
                StreamUtils.copy(s, UTF_8, stream);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
