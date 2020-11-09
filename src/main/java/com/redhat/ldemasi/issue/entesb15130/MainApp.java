package com.redhat.ldemasi.issue.entesb15130;

import org.apache.camel.main.Main;

import java.io.File;

import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.getFileFromResource;
import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.sendEmailWithAttachments;

/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {

        File [] attachments = { getFileFromResource("attach1/hello.txt"), getFileFromResource("attach2/hello.txt")};

        sendEmailWithAttachments("mymailserver.com", "587", "alice", "secret",
                "luigi@mymailserver.com", "Hello!", "Hello World", attachments);


        Main main = new Main();
        main.bind("myMailBinding", new AddIndexMailBindingStrategy());
        main.addRouteBuilder(new MyRouteBuilder());
        main.run(args);
    }

}

