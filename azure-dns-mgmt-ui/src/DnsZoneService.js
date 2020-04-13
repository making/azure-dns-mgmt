class DnsZoneService {
    loadDnsZones() {
        return fetch('/dns_zones')
            .then(res => res.json());
    }

    provisionDnsZone(name) {
        return fetch(`/dns_zones/${name}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json'
            }
        });
    }

    deleteDnsZone(name) {
        return fetch(`/dns_zones/${name}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });
    }
}

export default new DnsZoneService();