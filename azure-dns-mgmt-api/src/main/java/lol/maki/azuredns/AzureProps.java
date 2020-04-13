package lol.maki.azuredns;

import am.ik.csng.CompileSafeParameters;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import lol.maki.azuredns._AzurePropsParameters.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.io.File;
import java.util.function.Supplier;

@ConfigurationProperties(prefix = "azure")
public class AzureProps {
    private final String subscriptionId;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String location;
    private final String parentResourceGroup;
    private final String parentDnsZone;
    private final File workingDir;

    @ConstructorBinding
    @CompileSafeParameters
    public AzureProps(String subscriptionId, String tenantId, String clientId, String clientSecret, String location, String parentResourceGroup, String parentDnsZone, File workingDir) {
        this.subscriptionId = subscriptionId;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.location = location;
        this.parentResourceGroup = parentResourceGroup;
        this.parentDnsZone = parentDnsZone;
        this.workingDir = workingDir;
    }

    public String resourceGroup(String prefix) {
        return String.format("%s-%s", prefix, this.parentResourceGroup);
    }

    public String dnsZone(String prefix) {
        return String.format("%s.%s", prefix, this.parentDnsZone);
    }

    public <T> T configureLowerUnderscoreProps(Supplier<T> supplier, String keyPrefix, TriConsumer<T, String, String> consumer) {
        final T t = supplier.get();
        consumer.accept(t, keyPrefix + SubscriptionId.LOWER_UNDERSCORE, this.subscriptionId);
        consumer.accept(t, keyPrefix + TenantId.LOWER_UNDERSCORE, this.tenantId);
        consumer.accept(t, keyPrefix + ClientId.LOWER_UNDERSCORE, this.clientId);
        consumer.accept(t, keyPrefix + ClientSecret.LOWER_UNDERSCORE, this.clientSecret);
        consumer.accept(t, keyPrefix + Location.LOWER_UNDERSCORE, this.location);
        consumer.accept(t, keyPrefix + ParentResourceGroup.LOWER_UNDERSCORE, this.parentResourceGroup);
        consumer.accept(t, keyPrefix + ParentDnsZone.LOWER_UNDERSCORE, this.parentDnsZone);
        return t;
    }

    public <T> T configureUpperUnderscoreProps(Supplier<T> supplier, String keyPrefix, TriConsumer<T, String, String> consumer) {
        final T t = supplier.get();
        consumer.accept(t, keyPrefix + SubscriptionId.UPPER_UNDERSCORE, this.subscriptionId);
        consumer.accept(t, keyPrefix + TenantId.UPPER_UNDERSCORE, this.tenantId);
        consumer.accept(t, keyPrefix + ClientId.UPPER_UNDERSCORE, this.clientId);
        consumer.accept(t, keyPrefix + ClientSecret.UPPER_UNDERSCORE, this.clientSecret);
        consumer.accept(t, keyPrefix + Location.UPPER_UNDERSCORE, this.location);
        consumer.accept(t, keyPrefix + ParentResourceGroup.UPPER_UNDERSCORE, this.parentResourceGroup);
        consumer.accept(t, keyPrefix + ParentDnsZone.UPPER_UNDERSCORE, this.parentDnsZone);
        return t;
    }

    public DnsZoneManager dnsZoneManager() {
        final ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                this.clientId, this.tenantId, this.clientSecret, AzureEnvironment.AZURE);
        final RestClient restClient = new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .build();
        return DnsZoneManager.authenticate(restClient, this.subscriptionId);
    }

    public String redact(String s) {
        return s.replace(this.subscriptionId, "[REDACTED]")
                .replace(this.tenantId, "[REDACTED]")
                .replace(this.clientId, "[REDACTED]")
                .replace(this.clientSecret, "[REDACTED]");
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public static interface TriConsumer<T1, T2, T3> {
        void accept(T1 t1, T2 t2, T3 t3);
    }
}
