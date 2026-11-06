package com.fube.clientes.externos;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.model.ClearType;
import org.mockserver.serialization.ExpectationSerializer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;

class ValidadorEmailClienteJsonTest {

    private static final ExpectationSerializer SERIALIZER = new ExpectationSerializer(new MockServerLogger());

    private static ClientAndServer mockServer;

    private ValidadorEmailCliente validador;

    @BeforeAll
    static void iniciarMockServer() throws IOException {
        // Puerto 0: usa cualquier puerto libre
        mockServer = ClientAndServer.startClientAndServer(0);

        cargarExpectations("mockserver/validador-email-expectations.json");
    }

    @AfterAll
    static void detenerMockServer() {
        mockServer.stop();
    }

    @BeforeEach
    void setUp() {
        // Limpia solo los requests registrados, las expectations se cargan una vez en el BeforeAll
        mockServer.clear(request(), ClearType.LOG);
        validador = new ValidadorEmailCliente(RestClient.builder(),
                "http://localhost:" + mockServer.getPort());
    }

    @Test
    void devuelveTrueCuandoElServicioExternoValidaElEmail() {
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
        boolean resultado = validador.esValido("invalido@test.com");

        assertThat(resultado).isFalse();
    }

    @Test
    void lanzaExcepcionCuandoElServicioExternoFalla() {
        assertThatThrownBy(() -> validador.esValido("error@test.com"))
                .isInstanceOf(HttpServerErrorException.class);
    }

    // Lee un array de expectations (request + response) desde un json del classpath y lo registra en MockServer
    private static void cargarExpectations(String archivo) throws IOException {
        String json = new ClassPathResource(archivo).getContentAsString(StandardCharsets.UTF_8);
        Expectation[] expectations = SERIALIZER.deserializeArray(json, false);
        mockServer.upsert(expectations);
    }
}
