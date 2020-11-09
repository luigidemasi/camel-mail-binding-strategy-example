package com.redhat.ldemasi.issue.entesb15130;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.getAttachmentContent;
import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.sendEmailWithAttachments;
import static com.redhat.ldemasi.issue.entesb15130.MyMailUtils.getFileFromResource;

/**
 * Tests the custom  {@link org.apache.camel.component.mail.MailBinding} using 2 attachments with the same file name
 */
public class MyMailAttachmentsTest extends CamelTestSupport {

    protected final File FILE_PATH_1 =  getFileFromResource("attach1/file.txt");
    protected final File FILE_PATH_2 =  getFileFromResource("attach2/file.txt");
    protected String expectedFileContent1;
    protected String expectedFileContent2;
    protected final Pattern PATTERN = Pattern.compile("^\\d - file.txt");

    


    @Before
    public void setup() throws Exception {
        expectedFileContent1 = context.getTypeConverter().convertTo(String.class, FILE_PATH_1);
        expectedFileContent2 = context.getTypeConverter().convertTo(String.class, FILE_PATH_2);
        Mailbox.clearAll();
        // create the exchange with the mail message that is multipart with a file and a Hello World text/plain message.
        File [] attachments = {FILE_PATH_1, FILE_PATH_2};
        sendEmailWithAttachments("mymailserver.com", "587", "alice", "secret",
                "luigi@mymailserver.com", "Hello!", "Hello World", attachments);
    }

    @Test
    public void testSplitAttachments() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:mail");

        //We expect one mail message
        mock.expectedMessageCount(1);
        mock.assertIsSatisfied();

        Message message = mock.getReceivedExchanges().get(0).getIn();

        assertEquals(2, message.getAttachments().size());

        Iterator<String> keysIterator = message.getAttachments().keySet().iterator();

        String key1 = keysIterator.next();
        String key2 = keysIterator.next();

        Matcher matcher1 = PATTERN.matcher(key1);
        Matcher matcher2 = PATTERN.matcher(key2);

        assertTrue(matcher1.matches());
        assertTrue(matcher2.matches());

        String actualFileContent1 = getAttachmentContent(message.getAttachment(key1));
        String actualFileContent2 = getAttachmentContent(message.getAttachment(key2));

        boolean checkContent1 = expectedFileContent1.equals(actualFileContent1) || expectedFileContent1.equals(actualFileContent2);
        boolean checkContent2 = expectedFileContent2.equals(actualFileContent1) || expectedFileContent2.equals(actualFileContent2);

        assertTrue("Should have " + FILE_PATH_1.getCanonicalPath() + " file attachment", checkContent1);
        assertTrue("Should have " + FILE_PATH_2.getCanonicalPath() + " file attachment", checkContent2);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new MyRouteBuilder();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = new JndiRegistry(this.createJndiContext());
        registry.bind("myMailBinding", new AddIndexMailBindingStrategy());
        return registry;
    }
}

