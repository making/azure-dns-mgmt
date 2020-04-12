CREATE TABLE IF NOT EXISTS dns_zone
(
    name       VARCHAR(64) PRIMARY KEY,
    tfstate    TEXT         NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    INDEX dns_zone_created_by (created_by),
    INDEX dns_zone_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS certificate
(
    name       VARCHAR(64) PRIMARY KEY,
    lego       BLOB         NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    INDEX dns_zone_created_by (created_by),
    INDEX dns_zone_created_at (created_at),
    FOREIGN KEY dns_zone_certificate (name) REFERENCES dns_zone (name) ON DELETE CASCADE
);