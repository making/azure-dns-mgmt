package lol.maki.azuredns.dnszone;

import lol.maki.azuredns.AzureProps;
import lol.maki.azuredns.io.CommandRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class TerraformRunner {
    private final AzureProps azureProps;

    public TerraformRunner(AzureProps azureProps) {
        this.azureProps = azureProps;
    }

    public Flux<String> provision(String prefix) {
        final CommandRunner commandRunner = this.azureProps.configureLowerUnderscoreProps(() -> new CommandRunner(this.azureProps.getWorkingDir())
                        .addEnv("TF_VAR_prefix", prefix),
                "TF_VAR_", CommandRunner::addEnv);
        return Mono.fromCallable(() -> new ClassPathResource("terraform").getFile().getAbsolutePath())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(terraformDir -> commandRunner
                        .exec("bash", "-c", String.format("terraform init -no-color %s", terraformDir))
                        .concatWith(commandRunner.exec("bash", "-c", String.format("terraform plan -no-color -input=false -state=${TF_VAR_prefix}.tfstate -out=${TF_VAR_prefix}.tfplan %s", terraformDir)))
                        .concatWith(commandRunner.exec("bash", "-c", "terraform apply -no-color -input=false -state-out=${TF_VAR_prefix}.tfstate ${TF_VAR_prefix}.tfplan"))
                        .log("terraform"))
                .map(azureProps::redact);
    }

    public Flux<String> deprovision(String prefix) {
        final CommandRunner commandRunner = this.azureProps.configureLowerUnderscoreProps(() -> new CommandRunner(this.azureProps.getWorkingDir())
                        .addEnv("TF_VAR_prefix", prefix),
                "TF_VAR_", CommandRunner::addEnv);
        return Mono.fromCallable(() -> new ClassPathResource("terraform").getFile().getAbsolutePath())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(terraformDir -> commandRunner
                        .exec("bash", "-c", String.format("terraform init -no-color %s", terraformDir))
                        .concatWith(commandRunner.exec("bash", "-c", String.format("terraform destroy -no-color -force -state=${TF_VAR_prefix}.tfstate  %s", terraformDir)))
                        .log("terraform"))
                .map(azureProps::redact);
    }
}
