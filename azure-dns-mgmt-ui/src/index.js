const initialize = () => {
    const reloadButton = document.getElementById('reload');
    const dnsZones = document.getElementById('dnsZones');
    const reload = async () => {
        dnsZones.innerHTML = await loadDnsZones();
    };
    reloadButton.addEventListener('click', reload);
    reload().then();
    dnsZones.addEventListener('click', e => {
        const elm = e.target;
        if (elm.type === 'button' && elm.dataset && elm.dataset.name) {
            const name = elm.dataset.name;
            downloadCertificates(name);
        }
    });
};

const loadDnsZones = () => fetch('/dns_zones')
    .then(res => res.json())
    .then(data => data.map(zone => `<tr>
<td>${zone.name}</td>
<td>${zone.createdBy}</td>
<td>${zone.createdAt}</td>
<td>${zone.certificate && downloadCertificatesButton(zone.name)}</td>
</tr>`).join(''));


const downloadCertificates = (name) => {
    const a = document.createElement('a');
    a.href = `/certificates/${name}`;
    a.click();
};

const downloadCertificatesButton = (name) => {
    return `<span><button type="button" class="pui-btn pui-btn--primary" data-name="${name}">Download</button></span>`;
};

document.addEventListener('DOMContentLoaded', initialize);