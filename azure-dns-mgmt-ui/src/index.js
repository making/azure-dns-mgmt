const initialize = () => {
    const reloadButton = document.getElementById('reload');
    const dnsZones = document.getElementById('dnsZones');
    const reload = async () => {
        dnsZones.innerHTML = await loadDnsZones();
    };
    reloadButton.addEventListener('click', reload);
    reload().then();
};

const loadDnsZones = () => fetch('/dns_zones')
    .then(res => res.json())
    .then(data => data.map(zone => `<tr>
<td>${zone.name}</td>
<td>${zone.createdBy}</td>
<td>${zone.createdAt}</td>
<td>${zone.certificate}</td>
</tr>`).join(''));

document.addEventListener('DOMContentLoaded', initialize);