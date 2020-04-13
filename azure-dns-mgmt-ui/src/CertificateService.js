class CertificateService {
    generateCertificates(name) {
        return fetch(`/certificates/${name}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json'
            }
        });
    }
}

export default new CertificateService();