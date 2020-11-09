package com.redhat.ldemasi.issue.entesb15130;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.*;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(AddIndexMailBindingStrategy.class);

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    @Override
    public void configure() throws Exception {
        from("imap://luigi@mymailserver.com:587?binding=#myMailBinding&password=secret&consumer.initialDelay=100&consumer.delay=100")
                .to("log:email")
                .process(
                        exchange -> {
                            // log out the attachments
                            LOG.info("*** Attachments received: ***");
                            exchange.getIn()
                                    .getAttachments()
                                    .entrySet()
                                    .stream()
                                    .forEach(entry -> LOG.info("file: '{}' - content: '{}'", entry.getKey(), getAttachmentContent(entry.getValue())));
                        })
                .log("End...")
                .to("mock:mail");
    }
}
