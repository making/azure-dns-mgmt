package lol.maki.azuredns.certificate;

import lol.maki.azuredns.AzureProps;
import lol.maki.azuredns.io.CommandRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LegoRunner {
    private final AzureProps azureProps;

    public LegoRunner(AzureProps azureProps) {
        this.azureProps = azureProps;
    }

    public Flux<String> run(String prefix, String email) {
        final CommandRunner commandRunner = this.azureProps.configureUpperUnderscoreProps(() -> new CommandRunner(this.azureProps.getWorkingDir())
                        .addEnv("EMAIL", email),
                "AZURE_", CommandRunner::addEnv);
        final Flux<String> lego = commandRunner
                .exec("bash", "-c", "set -e\n" +
                        String.format("export SUBDOMAIN=%s.${AZURE_PARENT_DNS_ZONE}\n", prefix) +
                        String.format("export AZURE_RESOURCE_GROUP=%s-${AZURE_PARENT_RESOURCE_GROUP}\n", prefix) +
                        String.format("mkdir -p %s\n", prefix) +
                        String.format("cd %s\n", prefix) +
                        "lego --accept-tos \\\n" +
                        "  --key-type=rsa4096 \\\n" +
                        "  --domains=\"*.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.apps.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.sys.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.uaa.sys.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.login.sys.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.run.${SUBDOMAIN}\" \\\n" +
                        "  --domains=\"*.dev.${SUBDOMAIN}\" \\\n" +
                        "  --email=\"${EMAIL}\" \\\n" +
                        "  --dns=azure \\\n" +
                        "  run")
                .log("lego");
        final Flux<String> tar = commandRunner.exec("bash", "-c", "set -e\n" +
                String.format("cd %s/.lego\n", prefix) +
                "tar -czvf lego.tar.gz *")
                .log("tar");
        return lego.concatWith(tar);
    }
}
