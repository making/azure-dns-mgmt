const initialize = async () => {
    const reloadButton = document.getElementById('reload');
    const provisionButton = document.getElementById('provision');
    const dnsZones = document.getElementById('dnsZones');
    const dnsRecords = document.getElementById('dnsRecords');
    const prefix = document.getElementById('prefix');
    const reload = async () => {
        dnsZones.innerHTML = await loadDnsZones();
    };

    const onClickReload = async () => {
        const text = reloadButton.innerText;
        reloadButton.disabled = true;
        reloadButton.innerText = 'Reloading ...';
        dnsZones.innerHTML = await loadDnsZones()
        reloadButton.disabled = false;
        reloadButton.innerText = text;
    };

    const onClickProvision = async e => {
        const elm = e.target;
        const name = prefix.value;
        const text = elm.innerText;
        if (!name || name.trim().length === 0) {
            alert('"prefix" is required!');
            return;
        }
        elm.innerText = 'Provisioning ...';
        provisionButton.disabled = true;
        prefix.disabled = true;
        await provisionDnsZone(name);
        await reload();
        prefix.value = '';
        elm.innerText = text;
        provisionButton.disabled = false;
        prefix.disabled = false;
    };

    const onClickDnsZone = async e => {
        const elm = e.target;
        if (elm.type === 'button' && elm.dataset && elm.dataset.name && elm.dataset.command) {
            const name = elm.dataset.name;
            const command = elm.dataset.command;
            const text = elm.innerText;
            switch (command) {
                case 'download': {
                    downloadCertificates(name);
                    break;
                }
                case 'generate': {
                    elm.innerText = 'Generating ...';
                    elm.disabled = true;
                    await generateCertificates(name);
                    await reload();
                    break;
                }
                case 'delete': {
                    elm.innerText = 'Deleting ...';
                    elm.disabled = true;
                    await deleteDnsZone(name);
                    await reload();
                }
                case 'show-records': {
                    elm.innerText = 'Loading ...';
                    dnsRecords.innerHTML = `<h3>${name}</h3><p>Loading ...</p>`;
                    elm.disabled = true;
                    dnsRecords.innerHTML = await loadDnsRecords(name);
                    elm.innerText = text;
                    elm.disabled = false;
                }
            }
        }
    };
    reloadButton.addEventListener('click', onClickReload);
    provisionButton.addEventListener('click', onClickProvision);
    dnsZones.addEventListener('click', onClickDnsZone);
    await onClickReload();
};

const loadDnsZones = () => fetch('/dns_zones')
    .then(res => res.json())
    .then(data => data.map(zone => `<tr>
<td>${zone.name}</td>
<td>${zone.createdBy}</td>
<td>${zone.createdAt}</td>
<td>${showDnsRecordsButton(zone.name)}</td>
<td>${zone.certificate ? downloadCertificatesButton(zone.name) : generateCertificatesButton(zone.name)}</td>
<td>${deleteDnsZoneButton(zone.name)}</td>
</tr>`).join(''));


const provisionDnsZone = (name) => {
    return fetch(`/dns_zones/${name}`, {
        method: 'PUT',
        headers: {
            'Accept': 'application/json'
        }
    });
};

const loadDnsRecords = (name) => {
    return fetch(`/dns_zones/${name}/dns_records`)
        .then(res => res.json())
        .then(data => {
            const tbody = data.map(record => `<tr><td>${record.name}</td><td>${record.type}</td><td>${record.ttl}</td><td>${record.value.join('<br>')}</td></tr>`).join('');
            return `<h3>${name}</h3>
<table class="pui-table pui-table--tr-hover">
    <thead>
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th>TTL</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>${tbody}</tbody>
</table>`;
        });
};

const showDnsRecordsButton = (name) => {
    return `<span><button type="button" class="pui-btn pui-btn--default" data-name="${name}" data-command="show-records">Show Records</button></span>`;
};

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