package lol.maki.azuredns.dnszone;

import com.microsoft.azure.management.dns.*;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsRecordSet.UpdateDefinitionStages.*;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import lol.maki.azuredns.AzureProps;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

import java.util.List;

@Component
public class DnsRecordService {
    private final AzureProps azureProps;
    private final DnsZoneManager dnsZoneManager;

    public DnsRecordService(AzureProps azureProps) {
        this.azureProps = azureProps;
        this.dnsZoneManager = azureProps.dnsZoneManager();
    }

    public Flux<DnsRecord> findByPrefix(String prefix) {
        final String resourceGroup = this.azureProps.resourceGroup(prefix);
        final Mono<DnsZone> dnsZone = Mono.from(RxReactiveStreams.toPublisher(this.dnsZoneManager.zones()
                .listByResourceGroupAsync(resourceGroup)));
        return dnsZone.flatMapIterable(DnsZone::listRecordSets)
                .filter(rs -> rs instanceof ARecordSet || rs instanceof CNameRecordSet || rs instanceof NSRecordSet)
                .map(rs -> new DnsRecord(rs.name(), rs.recordType().name(), rs.timeToLive(), value(rs)));
    }

    public Mono<Void> add(String prefix, DnsRecord dnsRecord) {
        final String resourceGroup = this.azureProps.resourceGroup(prefix);
        final Mono<DnsZone> dnsZone = Mono.from(RxReactiveStreams.toPublisher(this.dnsZoneManager.zones()
                .listByResourceGroupAsync(resourceGroup)));
        return dnsZone
                .map(Updatable::update)
                .map(update -> toUpdate(update, dnsRecord).withTimeToLive(dnsRecord.getTtl()).attach())
                .flatMap(update -> Mono.from(RxReactiveStreams.toPublisher(update.applyAsync())))
                .then();
    }

    public Mono<Void> delete(String prefix, DnsRecord dnsRecord) {
        final String resourceGroup = this.azureProps.resourceGroup(prefix);
        final Mono<DnsZone> dnsZone = Mono.from(RxReactiveStreams.toPublisher(this.dnsZoneManager.zones()
                .listByResourceGroupAsync(resourceGroup)));
        return dnsZone
                .map(Updatable::update)
                .map(update -> toDelete(update, dnsRecord))
                .flatMap(update -> Mono.from(RxReactiveStreams.toPublisher(update.applyAsync())))
                .then();
    }

    public WithAttach<DnsZone.Update> toUpdate(DnsZone.Update update, DnsRecord dnsRecord) {
        final String type = dnsRecord.getType();
        if ("A".equalsIgnoreCase(type)) {
            final ARecordSetBlank<DnsZone.Update> recordSet = update.defineARecordSet(dnsRecord.getName());
            WithARecordIPv4AddressOrAttachable<DnsZone.Update> attachable = null;
            for (final String v : dnsRecord.getValue()) {
                attachable = (attachable == null ? recordSet : attachable).withIPv4Address(v);
            }
            return attachable;
        } else if ("CNAME".equalsIgnoreCase(type)) {
            return update.defineCNameRecordSet(dnsRecord.getName())
                    .withAlias(String.join("", dnsRecord.getValue()));
        } else if ("NS".equalsIgnoreCase(type)) {
            final NSRecordSetBlank<DnsZone.Update> recordSet = update.defineNSRecordSet(dnsRecord.getName());
            WithNSRecordNameServerOrAttachable<DnsZone.Update> attachable = null;
            for (final String v : dnsRecord.getValue()) {
                attachable = (attachable == null ? recordSet : attachable).withNameServer(v);
            }
            return attachable;
        }
        throw new IllegalArgumentException(String.format("Type '%s' is currently not supported!", type));
    }

    public DnsZone.Update toDelete(DnsZone.Update update, DnsRecord dnsRecord) {
        final String type = dnsRecord.getType();
        final String name = dnsRecord.getName();
        if ("A".equalsIgnoreCase(type)) {
            return update.withoutARecordSet(name);
        } else if ("CNAME".equalsIgnoreCase(type)) {
            return update.withoutCNameRecordSet(name);
        } else if ("NS".equalsIgnoreCase(type)) {
            return update.withoutNSRecordSet(name);
        }
        throw new IllegalArgumentException(String.format("Type '%s' is currently not supported!", type));
    }

    public static List<String> value(DnsRecordSet rs) {
        if (rs instanceof ARecordSet) {
            return ((ARecordSet) rs).ipv4Addresses();
        } else if (rs instanceof CNameRecordSet) {
            return List.of(((CNameRecordSet) rs).canonicalName());
        } else if (rs instanceof NSRecordSet) {
            return ((NSRecordSet) rs).nameServers();
        }
        return List.of();
    }
}
