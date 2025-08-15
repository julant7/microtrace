package com.julant7.microtrace.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "MicroTrace Service API",
                description = "API for Distributed Tracing System",
                version = "1.0.0",
                contact = @Contact(
                        name = "Julia \uD83D\uDC96",
                        email = "yulia.antonova225@gmail.com"
                )

        )
)
public class OpenApiConfig {
}
