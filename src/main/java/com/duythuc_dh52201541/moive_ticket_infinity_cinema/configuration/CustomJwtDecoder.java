package com.duythuc_dh52201541.moive_ticket_infinity_cinema.configuration;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.IntrospectResquest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.IntrospectRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {

        IntrospectRespone response = null;
        try {
            response = authenticationService.introspect(
                    IntrospectResquest.builder().token(token).build());
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }

        if (!response.isValid()) throw new JwtException("Token invalid");


        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
