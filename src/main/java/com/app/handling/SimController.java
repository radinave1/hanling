package com.app.handling;
;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class SimController {

    private static final Logger logger = LoggerFactory.getLogger(SimController.class);

    @GetMapping("/iccid")
    public String log() {
        logger.info("This is an info log message.");
        logger.error("This is an error log message.");
        return "Logs generated.";
    }

    @GetMapping("/activate/{simId}")
    public String log( @PathVariable String simId) {
        logger.info("This is an info log message.");
        logger.error("This is an error log message.");
        return "Logs generated.";
    }

}

