package lol.maki.azuredns.io;

import reactor.core.publisher.Mono;

public class ResultMessages {
    public static Mono<String> result(Mono<?> publisher) {
        return publisher.thenReturn("✅ Succeeded!")
                .onErrorReturn("❌ Failed!");
    }
}
