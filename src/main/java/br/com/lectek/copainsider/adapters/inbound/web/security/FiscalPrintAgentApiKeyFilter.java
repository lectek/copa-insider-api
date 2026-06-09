package br.com.lectek.copainsider.adapters.inbound.web.security;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.lectek.copainsider.application.service.fiscal.FiscalPrintStationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiscalPrintAgentApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_STATION_CODE = "X-Print-Station-Code";
    public static final String HEADER_STATION_KEY = "X-Print-Station-Key";

    private final ObjectProvider<FiscalPrintStationService>
            fiscalPrintStationServiceProvider;

    public FiscalPrintAgentApiKeyFilter(
            final ObjectProvider<FiscalPrintStationService>
                    fiscalPrintStationServiceProviderValue
    ) {
        this.fiscalPrintStationServiceProvider =
                fiscalPrintStationServiceProviderValue;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin/fiscal/impressao/agente/");
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final FiscalPrintStationService fiscalPrintStationService =
                fiscalPrintStationServiceProvider.getIfAvailable();
        if (fiscalPrintStationService == null) {
            filterChain.doFilter(request, response);
            return;
        }
        final Authentication existing =
                SecurityContextHolder.getContext().getAuthentication();
        if (shouldAuthenticate(existing)) {
            final String headerCode = request.getHeader(HEADER_STATION_CODE);
            final String headerKey = request.getHeader(HEADER_STATION_KEY);
            if (headerCode != null && headerKey != null && pathMatchesCode(request, headerCode)) {
                final Optional<FiscalPrintStationEntity> station =
                        fiscalPrintStationService.authenticateAgent(
                                headerCode,
                                headerKey
                        );
                station.ifPresent(value -> authenticate(request, value));
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldAuthenticate(final Authentication existing) {
        return existing == null
                || existing instanceof AnonymousAuthenticationToken
                || !existing.isAuthenticated();
    }

    private boolean pathMatchesCode(
            final HttpServletRequest request,
            final String headerCode
    ) {
        final String uri = request.getRequestURI();
        final String marker = "/estacoes/";
        final int start = uri.indexOf(marker);
        if (start < 0) {
            return true;
        }
        final int codeStart = start + marker.length();
        final int codeEnd = uri.indexOf('/', codeStart);
        final String pathCode = codeEnd >= 0
                ? uri.substring(codeStart, codeEnd)
                : uri.substring(codeStart);
        return pathCode.equalsIgnoreCase(headerCode);
    }

    private void authenticate(
            final HttpServletRequest request,
            final FiscalPrintStationEntity station
    ) {
        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "PRINT_AGENT:" + station.getCode(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_PRINT_AGENT"))
                );
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
