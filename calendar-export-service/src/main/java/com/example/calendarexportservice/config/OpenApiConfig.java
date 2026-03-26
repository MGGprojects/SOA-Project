package com.example.calendarexportservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI calendarExportServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8085");
        localServer.setDescription("Local development server");

        Contact contact = new Contact();
        contact.setName("Calendar Export Service Team");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Calendar Export Service API")
                .version("1.0.0")
                .description("Calendar Export Service API. "
                        + "This service provides endpoints for exporting events as .ics calendar files "
                        + "and generating calendar links for Google Calendar, Outlook, etc.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
