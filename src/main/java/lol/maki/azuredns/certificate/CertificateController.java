package lol.maki.azuredns.certificate;

import lol.maki.azuredns.AzureProps;
import lol.maki.azuredns.dnszone.DnsZoneRepository;
import lol.maki.azuredns.io.Resources;
import lol.maki.azuredns.io.ResultMessages;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(path = "certificates")
public class CertificateController {
    private final LegoRunner legoRunner;
    private final AzureProps azureProps;
    private final CertificateRepository certificateRepository;
    private final DnsZoneRepository dnsZoneRepository;

    public CertificateController(LegoRunner legoRunner, AzureProps azureProps, CertificateRepository certificateRepository, DnsZoneRepository dnsZoneRepository) {
        this.legoRunner = legoRunner;
        this.azureProps = azureProps;
        this.certificateRepository = certificateRepository;
        this.dnsZoneRepository = dnsZoneRepository;
    }

    @PutMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generate(@PathVariable String prefix, @AuthenticationPrincipal Authentication authentication) {
        final String name = this.azureProps.dnsZone(prefix);
        final String createdBy = authentication.getName();
        return this.dnsZoneRepository.findOne(name)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, "The requested certificate is not found: " + name)))
                .flatMapMany(__ -> this.legoRunner.run(prefix, createdBy)
                        .concatWith(Mono.defer(() -> {
                            final FileSystemResource tarFile = new FileSystemResource(String.format("%s/%s/.lego/lego.tar.gz", this.azureProps.getWorkingDir().getAbsolutePath(), prefix));
                            final Mono<Certificate> certificate = Resources.copyToByteArray(tarFile)
                                    .map(lego -> new Certificate(name, lego, createdBy, LocalDateTime.now()));
                            return this.certificateRepository.save(certificate)
                                    .transform(ResultMessages::result);
                        })));
    }

    @GetMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> download(@PathVariable String prefix) {
        final String name = this.azureProps.dnsZone(prefix);
        return this.certificateRepository.findOne(name)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, "The requested certificate is not found: " + name)))
                .map(Certificate::getLego)
                .map(body -> ResponseEntity
                        .ok().header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.tar.gz", name))
                        .body(body));
    }

    @DeleteMapping(path = {"{prefix}", "{prefix}.${azure.parent-dns-zone}"})
    public Mono<Void> delete(@PathVariable String prefix) {
        final String name = this.azureProps.dnsZone(prefix);
        return this.certificateRepository.delete(name);
    }
}
