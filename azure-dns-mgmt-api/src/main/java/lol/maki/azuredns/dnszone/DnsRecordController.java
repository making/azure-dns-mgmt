package lol.maki.azuredns.dnszone;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "dns_zones")
public class DnsRecordController {
    private final DnsRecordService dnsRecordService;

    public DnsRecordController(DnsRecordService dnsRecordService) {
        this.dnsRecordService = dnsRecordService;
    }

    @GetMapping(path = {"{prefix}/dns_records", "{prefix}.${azure.parent-dns-zone}/dns_records"})
    public Flux<DnsRecord> getDnsRecords(@PathVariable String prefix) {
        return this.dnsRecordService.findByPrefix(prefix);
    }

    @PostMapping(path = {"{prefix}/dns_records", "{prefix}.${azure.parent-dns-zone}/dns_records"})
    public Mono<Void> postDnsRecords(@PathVariable String prefix, @RequestBody DnsRecord dnsRecord) {
        return this.dnsRecordService.add(prefix, dnsRecord);
    }

    @DeleteMapping(path = {"{prefix}/dns_records", "{prefix}.${azure.parent-dns-zone}/dns_records"})
    public Mono<Void> deleteDnsRecord(@PathVariable String prefix, @RequestBody DnsRecord dnsRecord) {
        return this.dnsRecordService.delete(prefix, dnsRecord);
    }
}
