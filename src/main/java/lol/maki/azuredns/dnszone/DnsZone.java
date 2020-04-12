package lol.maki.azuredns.dnszone;

import am.ik.csng.CompileSafeParameters;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class DnsZone {
    @Id
    private final String name;
    private final String tfstate;
    private final String createdBy;
    private final LocalDateTime createdAt;

    @CompileSafeParameters
    public DnsZone(String name, String tfstate, String createdBy, LocalDateTime createdAt) {
        this.name = name;
        this.tfstate = tfstate;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonIgnore
    public String getTfstate() {
        return tfstate;
    }

    @Override
    public String toString() {
        return "DnsZone{" +
                "name='" + name + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
