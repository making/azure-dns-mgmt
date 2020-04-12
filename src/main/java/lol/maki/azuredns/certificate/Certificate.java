package lol.maki.azuredns.certificate;

import am.ik.csng.CompileSafeParameters;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class Certificate {
    @Id
    private final String name;
    private final byte[] lego;
    private final String createdBy;
    private final LocalDateTime createdAt;

    @CompileSafeParameters
    public Certificate(String name, byte[] lego, String createdBy, LocalDateTime createdAt) {
        this.name = name;
        this.lego = lego;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public byte[] getLego() {
        return lego;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "name='" + name + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
