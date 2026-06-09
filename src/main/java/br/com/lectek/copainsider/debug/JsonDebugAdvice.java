package br.com.lectek.copainsider.debug;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Profile({"dev", "docker"})
@ControllerAdvice
public class JsonDebugAdvice {
    private static final Logger log = LoggerFactory.getLogger(JsonDebugAdvice.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    void onNotReadable(HttpMessageNotReadableException ex) {
        Throwable root = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause() : ex;
        if (root instanceof JsonMappingException jme) {
            log.error("JSON parse failed at {}: {}", jme.getPathReference(), root.toString(), ex);
        } else {
            log.error("JSON parse failed: {}", root.toString(), ex);
        }
    }
}
