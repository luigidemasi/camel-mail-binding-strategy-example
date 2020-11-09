package com.redhat.ldemasi.issue.entesb15130;

import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Needed if you are using camel 2.21, from camel 2.23 you can use directly
 * org.apache.camel.component.mail.DelegatingDataSource
 */

public final class DelegatingDataSource implements DataSource {

    private final DataSource delegate;

    private final String name;

    public DelegatingDataSource(final String name, final DataSource delegate) {
        this.name = StringHelper.notEmpty(name, "name");
        this.delegate = ObjectHelper.notNull(delegate, "DataSource");
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

}
