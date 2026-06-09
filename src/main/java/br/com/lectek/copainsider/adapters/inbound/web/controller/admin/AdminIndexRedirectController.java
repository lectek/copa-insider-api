package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

@Profile("!test")
@Controller
@RequestMapping("/admin")
public class AdminIndexRedirectController {

    /**
     * Default admin destination.
     */
    private static final String TARGET = "/admin/dashboard";

    /**
     * Redirects GET and HEAD on /admin to dashboard.
     *
     * @return redirect view name
     */
    @RequestMapping(
            path = {"", "/"},
            method = {RequestMethod.GET, RequestMethod.HEAD}
    )
    public String redirectGet() {
        return "redirect:" + TARGET;
    }

    /**
     * Redirects POST on /admin to dashboard.
     *
     * @return redirect view
     */
    @PostMapping({"", "/"})
    public RedirectView redirectPost() {
        final RedirectView redirectView = new RedirectView(TARGET);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }

    /**
     * Redirects non-GET methods to dashboard preserving explicit headers.
     *
     * @param request current request
     * @return redirect response
     */
    @RequestMapping(
            path = {"", "/"},
            method = {
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS,
                    RequestMethod.TRACE
            }
    )
    public ResponseEntity<Void> redirectOthers(
            final HttpServletRequest request
    ) {
        final String location = request.getContextPath() + TARGET;
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .header(HttpHeaders.LOCATION, location)
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, must-revalidate, max-age=0"
                )
                .header("Pragma", "no-cache")
                .header("Referrer-Policy", "no-referrer")
                .build();
    }
}
