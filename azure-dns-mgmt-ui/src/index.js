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
        if (elm.type === 'button' && elm.dataset && elm.dataset.name && elm.dataset.command) {
            const name = elm.dataset.name;
            const command = elm.dataset.command;
            switch (command) {
                case 'download': {
                    downloadCertificates(name);
                    break;
                }
                case 'generate': {
                    elm.innerText = 'Generating ...';
                    elm.disabled = true;
                    generateCertificates(name)
                        .then(() => reload().then());
                    break;
                }
                case 'delete': {
                    elm.innerText = 'Deleting ...';
                    elm.disabled = true;
                    deleteDnsZone(name)
                        .then(() => reload().then());
                }
            }
        }
    });
};

const loadDnsZones = () => fetch('/dns_zones')
    .then(res => res.json())
    .then(data => data.map(zone => `<tr>
<td>${zone.name}</td>
<td>${zone.createdBy}</td>
<td>${zone.createdAt}</td>
<td>${zone.certificate ? downloadCertificatesButton(zone.name) : generateCertificatesButton(zone.name)}</td>
<td>${deleteDnsZoneButton(zone.name)}</td>
</tr>`).join(''));


const downloadCertificates = (name) => {
    const a = document.createElement('a');
    a.href = `/certificates/${name}`;
    a.click();
};

const downloadCertificatesButton = (name) => {
    return `<span><button type="button" class="pui-btn pui-btn--primary" data-name="${name}" data-command="download">Download</button></span>`;
};

const generateCertificates = (name) => {
    return fetch(`/certificates/${name}`, {
        method: 'PUT',
        headers: {
            'Accept': 'application/json'
        }
    });
};

const generateCertificatesButton = (name) => {
    return `<span><button type="button" class="pui-btn pui-btn--brand" data-name="${name}" data-command="generate">Generate</button></span>`;
};

const deleteDnsZone = (name) => {
    return fetch(`/dns_zones/${name}`, {
        method: 'DELETE',
        headers: {
            'Accept': 'application/json'
        }
    });
};

const deleteDnsZoneButton = (name) => {
    return `<span><button type="button" class="pui-btn pui-btn--danger" data-name="${name}" data-command="delete">Delete</button></span>`;
};

document.addEventListener('DOMContentLoaded', initialize);