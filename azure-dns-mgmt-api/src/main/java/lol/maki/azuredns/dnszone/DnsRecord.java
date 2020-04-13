package lol.maki.azuredns.dnszone;

import am.ik.csng.CompileSafeParameters;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lol.maki.azuredns.dnszone._DnsRecordParameters.Ttl;
import lol.maki.azuredns.dnszone._DnsRecordParameters.Type;
import lol.maki.azuredns.dnszone._DnsRecordParameters.Value;
import lol.maki.azuredns.dnszone._DnsZoneParameters.Name;

import java.util.List;

public class DnsRecord {
    private final String name;
    private final String type;
    private final long ttl;
    private final List<String> value;

    @JsonCreator
    @CompileSafeParameters
    public DnsRecord(@JsonProperty(Name.LOWER_CAMEL) String name,
                     @JsonProperty(Type.LOWER_CAMEL) String type,
                     @JsonProperty(Ttl.LOWER_CAMEL) long ttl,
                     @JsonProperty(Value.LOWER_CAMEL) List<String> value) {
        this.name = name;
        this.type = type;
        this.ttl = ttl;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getTtl() {
        return ttl;
    }

    public List<String> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DnsRecord{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", ttl=" + ttl +
                ", value=" + value +
                '}';
    }
}
