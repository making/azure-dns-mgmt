package lol.maki.azuredns.dnszone;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class DnsZoneDto {
    @JsonUnwrapped
    private final DnsZone dnsZone;
    private final boolean certificate;

    public DnsZoneDto(DnsZone dnsZone, boolean certificate) {
        this.dnsZone = dnsZone;
        this.certificate = certificate;
    }

    public DnsZone getDnsZone() {
        return dnsZone;
    }

    public boolean isCertificate() {
        return certificate;
    }
}
