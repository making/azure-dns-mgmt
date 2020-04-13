package lol.maki.azuredns.dnszone;

import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.*;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
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
