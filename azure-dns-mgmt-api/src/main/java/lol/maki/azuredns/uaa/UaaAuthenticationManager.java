package lol.maki.azuredns.uaa;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class UaaAuthenticationManager implements ReactiveAuthenticationManager {
    private final WebClient webClient;

    public UaaAuthenticationManager(WebClient.Builder builder, UaaProps uaaProps) {
        this.webClient = builder
                .baseUrl(uaaProps.getUrl())
                .defaultHeaders(headers -> headers.setBasicAuth(uaaProps.getClientId(), uaaProps.getClientSecret()))
                .build();
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        final String username = Objects.toString(authentication.getPrincipal());
        final String password = Objects.toString(authentication.getCredentials());
        final LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>() {
            {
                add("username", username);
                add("password", password);
                add("grant_type", "password");
                add("scope", "openid");
            }
        };
        return this.webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .exchange()
                .flatMap(res -> res.toEntity(JsonNode.class)
                        .flatMap(entity -> {
                            if (entity.getStatusCode().is2xxSuccessful()) {
                                return Mono.fromCallable(() -> {
                                    final JWT idToken = JWTParser.parse(entity.getBody().get("id_token").asText());
                                    final JWTClaimsSet claims = idToken.getJWTClaimsSet();
                                    final String email = claims.getStringClaim("email");
                                    final List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
                                    return new UsernamePasswordAuthenticationToken(email, "", authorities);
                                });
                            } else {
                                return Mono.error(new BadCredentialsException(entity.getBody().get("error_description").asText()));
                            }
                        }))
                .cast(Authentication.class);
    }
}
