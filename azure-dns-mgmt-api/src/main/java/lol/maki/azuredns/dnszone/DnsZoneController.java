package lol.maki.azuredns.dnszone;

import lol.maki.azuredns.AzureProps;
import lol.maki.azuredns.io.Resources;
import lol.maki.azuredns.io.ResultMessages;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(path = "dns_zones")
public class DnsZoneController {
    private final AzureProps azureProps;
    private final TerraformRunner terraformRunner;
    private final DnsZoneRepository dnsZoneRepository;

    public DnsZoneController(AzureProps azureProps, TerraformRunner terraformRunner, DnsZoneRepository dnsZoneRepository) {
        this.azureProps = azureProps;
        this.terraformRunner = terraformRunner;
        this.dnsZoneRepository = dnsZoneRepository;
    }

    @GetMapping(path = "")
    public Flux<DnsZoneDto> getDnsZones() {
        return this.dnsZoneRepository.findOrderByCreatedAtDesc();
    }

    @GetMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"})
    public Mono<DnsZone> getDnsZone(@PathVariable String prefix) {
        final String name = this.azureProps.dnsZone(prefix);
        return this.dnsZoneRepository.findOne(name)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, "The requested dns zone is not found: " + name)));
    }

    @PutMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> provisionStream(@PathVariable String prefix, @AuthenticationPrincipal Authentication authentication) {
        return this.terraformRunner.provision(prefix)
                .concatWith(this.saveDnsZone(prefix, authentication.getName())
                        .transform(ResultMessages::result));
    }

    @PutMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<?> provisionJson(@PathVariable String prefix, @AuthenticationPrincipal Authentication authentication) {
        return this.terraformRunner.provision(prefix)
                .then(this.saveDnsZone(prefix, authentication.getName())
                        .transform(ResultMessages::result)
                        .map(result -> Map.of("result", result)));
    }

    private Mono<DnsZone> saveDnsZone(String prefix, String createdBy) {
        final String name = this.azureProps.dnsZone(prefix);
        return Mono.defer(() -> {
            final FileSystemResource file = new FileSystemResource(String.format("%s/%s.tfstate", this.azureProps.getWorkingDir().getAbsolutePath(), prefix));
            final Mono<DnsZone> dnsZone = Resources.copyToString(file)
                    .map(tfstate -> new DnsZone(name, tfstate, createdBy, LocalDateTime.now()));
            return this.dnsZoneRepository.save(dnsZone);
        });
    }

    @DeleteMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> deprovisionStream(@PathVariable String prefix) {
        final String name = this.azureProps.dnsZone(prefix);
        return this.deleteDnsZone(prefix).thenReturn("")
                .concatWith(this.terraformRunner.deprovision(prefix))
                .concatWith(this.dnsZoneRepository.delete(name)
                        .transform(ResultMessages::result));
    }

    @DeleteMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<?> deprovisionJson(@PathVariable String prefix) {
        final String name = this.azureProps.dnsZone(prefix);
        return this.deleteDnsZone(prefix)
                .thenMany(this.terraformRunner.deprovision(prefix))
                .then(this.dnsZoneRepository.delete(name)
                        .transform(ResultMessages::result)
                        .map(result -> Map.of("result", result)));
    }

    private Mono<Void> deleteDnsZone(String prefix) {
        final FileSystemResource file = new FileSystemResource(String.format("%s/%s.tfstate", this.azureProps.getWorkingDir().getAbsolutePath(), prefix));
        final String name = this.azureProps.dnsZone(prefix);
        return this.dnsZoneRepository.findOne(name)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, "The requested dns zone is not found: " + name)))
                .doOnSuccess(__ -> FileSystemUtils.deleteRecursively(file.getFile()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(DnsZone::getTfstate)
                .flatMap(tfstate -> Resources.copy(tfstate, file));
    }
}
