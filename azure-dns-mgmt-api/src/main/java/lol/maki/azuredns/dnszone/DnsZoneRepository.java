package lol.maki.azuredns.dnszone;

import lol.maki.azuredns.certificate._CertificateParameters;
import lol.maki.azuredns.dnszone._DnsZoneParameters.CreatedAt;
import lol.maki.azuredns.dnszone._DnsZoneParameters.CreatedBy;
import lol.maki.azuredns.dnszone._DnsZoneParameters.Name;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
public class DnsZoneRepository {
    private final DatabaseClient databaseClient;

    public DnsZoneRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<DnsZone> findOne(String name) {
        return this.databaseClient.select()
                .from(DnsZone.class)
                .matching(where(Name.LOWER_UNDERSCORE).is(name))
                .fetch()
                .one();
    }

    public Flux<DnsZoneDto> findOrderByCreatedAtDesc() {
        return this.databaseClient.execute("SELECT d.name, d.created_by, d.created_at, c.name AS certificate FROM dns_zone AS d LEFT JOIN certificate AS c ON d.name = c.name ORDER BY d.created_at DESC")
                .map(row -> {
                    final DnsZone dnsZone = new DnsZone(
                            row.get(Name.LOWER_UNDERSCORE, String.class),
                            null,
                            row.get(CreatedBy.LOWER_UNDERSCORE, String.class),
                            row.get(CreatedAt.LOWER_UNDERSCORE, LocalDateTime.class));
                    return new DnsZoneDto(dnsZone, row.get(_CertificateParameters.LOWER_UNDERSCORE, String.class) != null);
                })
                .all();
    }

    @Transactional
    public Mono<DnsZone> save(Mono<DnsZone> dnsZone) {
        return dnsZone.flatMap(v -> this.databaseClient.insert()
                .into(DnsZone.class)
                .using(v)
                .then()
                .thenReturn(v));
    }

    @Transactional
    public Mono<Void> delete(String name) {
        return this.databaseClient.delete()
                .from(DnsZone.class)
                .matching(where(Name.LOWER_UNDERSCORE).is(name))
                .then();
    }
}
