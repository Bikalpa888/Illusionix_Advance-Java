package com.virinchi.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleAnyException(Exception ex, HttpServletRequest req) {
        String traceId = (String) req.getAttribute(RequestCorrelationFilter.TRACE_ID);
        String path = req.getRequestURI();
        log.error("Unhandled exception at path={} traceId={}", path, traceId, ex);

        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "ok", false,
                            "error", "internal_error",
                            "message", "Something went wrong",
                            "traceId", traceId
                    ));
        }

        ModelMap model = new ModelMap();
        model.addAttribute("status", 500);
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Try again or contact support.");
        model.addAttribute("traceId", traceId);
        return new ModelAndView("error/500", model, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

