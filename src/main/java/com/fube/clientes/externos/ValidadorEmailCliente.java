package com.fube.clientes.externos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ValidadorEmailCliente {

    private final RestClient restClient;

    public ValidadorEmailCliente(RestClient.Builder builder,
            @Value("${validador.url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    public boolean esValido(String email) {
        ValidacionEmailResponse respuesta = restClient.get()
                .uri("/validar?email={email}", email)
                .retrieve()
                .body(ValidacionEmailResponse.class);
        return respuesta != null && respuesta.valido();
    }
}
