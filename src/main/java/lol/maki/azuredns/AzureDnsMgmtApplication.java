package lol.maki.azuredns;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AzureDnsMgmtApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureDnsMgmtApplication.class, args);
    }


}
