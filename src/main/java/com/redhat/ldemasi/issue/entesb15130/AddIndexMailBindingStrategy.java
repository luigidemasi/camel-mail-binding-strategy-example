package com.redhat.ldemasi.issue.entesb15130;

import org.apache.camel.Attachment;
import org.apache.camel.component.mail.MailBinding;
import org.apache.camel.impl.DefaultAttachment;
import org.apache.camel.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

public class AddIndexMailBindingStrategy extends MailBinding{

    private Logger LOG = LoggerFactory.getLogger(AddIndexMailBindingStrategy.class);


    private boolean validDisposition(String disposition, String fileName) {
        return disposition != null
                && fileName != null
                && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE));
    }

    @Override
    protected void extractAttachmentsFromMultipart(Multipart mp, Map<String, Attachment> map)
            throws MessagingException, IOException {

        String key;
        int index;

        for (int i = 0; i < mp.getCount(); i++) {
            Part part = mp.getBodyPart(i);
            LOG.trace("Part #{}: {}", i, part);

            if (part.isMimeType("multipart/*")) {
                LOG.trace("Part #{}: is mimetype: multipart/*", i);
                extractAttachmentsFromMultipart((Multipart) part.getContent(), map);
            } else {
                String disposition = part.getDisposition();
                String fileName = FileUtil.stripPath(part.getFileName());

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Part #{}: Disposition: {}", i, disposition);
                    LOG.trace("Part #{}: Description: {}", i, part.getDescription());
                    LOG.trace("Part #{}: ContentType: {}", i, part.getContentType());
                    LOG.trace("Part #{}: FileName: {}", i, fileName);
                    LOG.trace("Part #{}: Size: {}", i, part.getSize());
                    LOG.trace("Part #{}: LineCount: {}", i, part.getLineCount());
                }

                if (validDisposition(disposition, fileName) || fileName != null) {
                    LOG.debug("Mail contains file attachment: {}", fileName);

                    // use a customized key for the attachment map:
                    // instead of simple filename we use use "$index - $filename" as a key

                    index = map.size();
                    key = index + " - "+ fileName;

                    // Parts marked with a disposition of Part.ATTACHMENT are clearly attachments
                    final DataHandler dataHandler = part.getDataHandler();
                    final DataSource dataSource = dataHandler.getDataSource();

                    final DataHandler replacement = new DataHandler(new DelegatingDataSource(fileName, dataSource));
                    DefaultAttachment camelAttachment = new DefaultAttachment(replacement);
                    @SuppressWarnings("unchecked")
                    Enumeration<Header> headers = part.getAllHeaders();
                    while (headers.hasMoreElements()) {
                        Header header = headers.nextElement();
                        camelAttachment.addHeader(header.getName(), header.getValue());
                    }
                    map.put(key, camelAttachment);
                }
            }
        }
    }
}
