package lol.maki.azuredns.certificate;

import lol.maki.azuredns.certificate._CertificateParameters.Name;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
public class CertificateRepository {
    private final DatabaseClient databaseClient;

    public CertificateRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Certificate> findOne(String name) {
        return this.databaseClient.select()
                .from(Certificate.class)
                .matching(where(Name.LOWER_UNDERSCORE).is(name))
                .fetch()
                .one();
    }

    @Transactional
    public Mono<Certificate> save(Mono<Certificate> dnsZone) {
        return dnsZone.flatMap(v -> this.databaseClient.insert()
                .into(Certificate.class)
                .using(v)
                .then()
                .thenReturn(v));
    }

    @Transactional
    public Mono<Void> delete(String name) {
        return this.databaseClient.delete()
                .from(Certificate.class)
                .matching(where(Name.LOWER_UNDERSCORE).is(name))
                .then();
    }
}
