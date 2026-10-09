package com.tmsbackend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Lets the Modbus gateway service push readings without a user login: it
// polls the hardware around the clock on the plant PC, independent of any
// browser, so there's no user JWT to send. A request to the readings ingest
// endpoint carrying the shared X-Ingest-Key (INGEST_API_KEY here, the same
// value as TMS_INGEST_KEY on the gateway) is authenticated as the
// "gateway-ingest" service. The key unlocks nothing else - every other
// endpoint still requires a user's JWT.
@Component
public class IngestKeyAuthenticationFilter extends OncePerRequestFilter {
    static final String INGEST_PATH = "/tms/api/readings/batch";
    private static final String HEADER = "X-Ingest-Key";

    private final byte[] expectedKey;

    public IngestKeyAuthenticationFilter(@Value("${tms.ingest.api-key}") String apiKey) {
        this.expectedKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided != null && INGEST_PATH.equals(request.getRequestURI())
                && MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), expectedKey)) {
            var authentication = new UsernamePasswordAuthenticationToken("gateway-ingest", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
