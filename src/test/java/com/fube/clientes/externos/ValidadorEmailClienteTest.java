package com.fube.clientes.externos;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class ValidadorEmailClienteTest {

    private static ClientAndServer mockServer;

    private ValidadorEmailCliente validador;

    @BeforeAll
    static void iniciarMockServer() {
        // Puerto 0: usa cualquier puerto libre
        mockServer = ClientAndServer.startClientAndServer(0);
    }

    @AfterAll
    static void detenerMockServer() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
        validador = new ValidadorEmailCliente(RestClient.builder(),
                "http://localhost:" + mockServer.getPort());
    }

    @Test
    void devuelveTrueCuandoElServicioExternoValidaElEmail() {
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/validar")
                        .withQueryStringParameter("email", "ana.ggomez@test.com"))
                .respond(response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"email\": \"ana.gomez@test.com\", \"valido\": true}"));

        boolean resultado = validador.esValido("ana.gomez@test.com");

        assertThat(resultado).isTrue();

        mockServer.verify(request()
                        .withMethod("GET")
                        .withPath("/validar")
                        .withQueryStringParameter("email", "ana.gomez@test.com"),
                VerificationTimes.exactly(1));
    }

    @Test
    void devuelveFalseCuandoElServicioExternoRechazaElEmail() {
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/validar")
                        .withQueryStringParameter("email", "invalido@test.com"))
                .respond(response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"email\": \"invalido@test.com\", \"valido\": false}"));

        boolean resultado = validador.esValido("invalido@test.com");

        assertThat(resultado).isFalse();
    }

    @Test
    void lanzaExcepcionCuandoElServicioExternoFalla() {
        mockServer.when(request()
                        .withMethod("GET")
                        .withPath("/validar"))
                .respond(response().withStatusCode(500));

        assertThatThrownBy(() -> validador.esValido("ana.gomez@test.com"))
                .isInstanceOf(HttpServerErrorException.class);
    }
}
