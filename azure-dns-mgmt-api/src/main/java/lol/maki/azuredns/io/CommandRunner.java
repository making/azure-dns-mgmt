package lol.maki.azuredns.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class CommandRunner {
    private final Logger log = LoggerFactory.getLogger(CommandRunner.class);
    private final File workingDir;
    private final Map<String, String> extraEnv = new HashMap<>();

    public CommandRunner(File workingDir) {
        this.workingDir = workingDir;
    }

    public CommandRunner addEnv(String var, String val) {
        this.extraEnv.put(var, val);
        return this;
    }

    public Flux<String> exec(String... command) {
        return Flux.defer(() -> {
            try {
                final String commandPrompt = "(" + this.workingDir + ") " + Arrays.stream(command).reduce("$", (a, b) -> a + " " + b);
                log.info(commandPrompt);
                final ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(this.workingDir);
                if (!this.extraEnv.isEmpty()) {
                    Map<String, String> env = processBuilder.environment();
                    for (Entry<String, String> extra : this.extraEnv.entrySet()) {
                        env.put(extra.getKey(), extra.getValue());
                    }
                }
                return Mono.fromCallable(processBuilder::start)
                        .flatMapMany(process -> {
                            final Stream<String> stderr = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines();
                            final Flux<String> err = Flux.fromStream(stderr)
                                    .doFinally(__ -> stderr.close());
                            final Stream<String> stdout = new BufferedReader(new InputStreamReader(process.getInputStream())).lines();
                            final Flux<String> out = Flux.fromStream(stdout)
                                    .doFinally(__ -> stdout.close());
                            final Mono<Integer> exitCode = Mono.fromCallable(process::waitFor);
                            return err.mergeWith(out)
                                    .concatWith(exitCode.flatMap(c -> c == 0 ? Mono.empty() : Mono.error(new RuntimeException("Non zero exit code from process: " + c))));
                        })
                        .subscribeOn(Schedulers.boundedElastic());
            } catch (Exception e) {
                return Flux.error(e);
            }
        });
    }
}