package fr.epita.services;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.BasicConfigurator;

public class EmailSender {

    public static void sendEmails() throws Exception {

        BasicConfigurator.configure();

        String inputPath = "file:src/main/email-spool";
        String outputPath = "file:src/main/email-out";

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from(inputPath)
                .to(outputPath);
            }
        });

        context.start();
        Thread.sleep(5000);
        context.stop();
    }
}