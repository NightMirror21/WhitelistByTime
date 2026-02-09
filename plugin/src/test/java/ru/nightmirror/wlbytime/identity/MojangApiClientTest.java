package ru.nightmirror.wlbytime.identity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MojangApiClientTest {

    @Test
    public void lookupUuidReturnsParsedUuidAndCaches() throws Exception {
        MojangApiClient client = new MojangApiClient(1000, true, 60000);
        AtomicInteger calls = new AtomicInteger();
        FakeHttpClient httpClient = new FakeHttpClient(() -> {
            calls.incrementAndGet();
            return new FakeHttpResponse(200, "{\"id\":\"12345678123412341234123456789012\"}");
        });
        setHttpClient(client, httpClient);

        Optional<UUID> first = client.lookupUuid("Steve");
        Optional<UUID> second = client.lookupUuid("Steve");

        assertTrue(first.isPresent());
        assertEquals(first, second);
        assertEquals(1, calls.get());
    }

    @Test
    public void lookupUuidReturnsEmptyOnBadResponse() throws Exception {
        MojangApiClient client = new MojangApiClient(1000, false, 60000);
        FakeHttpClient httpClient = new FakeHttpClient(() -> new FakeHttpResponse(500, "{}"));
        setHttpClient(client, httpClient);

        Optional<UUID> result = client.lookupUuid("Steve");

        assertTrue(result.isEmpty());
    }

    private static void setHttpClient(MojangApiClient client, HttpClient httpClient) throws Exception {
        Field field = MojangApiClient.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(client, httpClient);
    }

    private static final class FakeHttpClient extends HttpClient {
        private final ResponseSupplier supplier;

        private FakeHttpClient(ResponseSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            HttpResponse<String> response = supplier.get();
            return (HttpResponse<T>) response;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }
    }

    private interface ResponseSupplier {
        HttpResponse<String> get();
    }

    private static final class FakeHttpResponse implements HttpResponse<String> {
        private final int status;
        private final String body;

        private FakeHttpResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return status;
        }

        @Override
        public HttpRequest request() {
            return HttpRequest.newBuilder(URI.create("http://localhost")).build();
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (a, b) -> true);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("http://localhost");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
