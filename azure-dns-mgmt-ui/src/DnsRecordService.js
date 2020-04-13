class DnsRecordService {
    loadDnsRecords(dnsZone) {
        return fetch(`/dns_zones/${dnsZone}/dns_records`)
            .then(res => res.json());
    }

    addDnsRecords(dnsZone, {name, type, ttl, value}) {
        return fetch(`/dns_zones/${dnsZone}/dns_records`, {
            method: 'POST',
            body: JSON.stringify({name, type, ttl, value}), // data can be `string` or {object}!
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }

    deleteDnsRecords(dnsZone, {name, type}) {
        return fetch(`/dns_zones/${dnsZone}/dns_records`, {
            method: 'DELETE',
            body: JSON.stringify({name, type}), // data can be `string` or {object}!
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }
}

export default new DnsRecordService();