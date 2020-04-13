package lol.maki.azuredns.dnszone;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
}
