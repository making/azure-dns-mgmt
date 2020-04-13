class DnsRecordService {
    loadDnsRecords(name) {
        return fetch(`/dns_zones/${name}/dns_records`)
            .then(res => res.json());
    }

    addDnsRecords({name, type, ttl, value}) {
        return fetch(`/dns_zones/${name}/dns_records`, {
            method: 'POST',
            body: JSON.stringify({name, type, ttl, value}), // data can be `string` or {object}!
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(res => res.json());
    }

    deleteDnsRecords({name, type}) {
        return fetch(`/dns_zones/${name}/dns_records`, {
            method: 'DELETE',
            body: JSON.stringify({name, type}), // data can be `string` or {object}!
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(res => res.json());
    }
}

export default new DnsRecordService();