package com.spdb.fdev.transport;

import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by xxx on 2018/5/14.
 */
public class SecurityUrlResource extends UrlResource {

    private UrlResource urlResource;

    public SecurityUrlResource(URL url) {
        super(url);
        this.urlResource = new UrlResource(url);
    }

    public InputStream getInputStream() throws IOException {
        URL url = this.urlResource.getURL();
        URLConnection con = url.openConnection();
        if (null != url.getAuthority() && url.getAuthority().contains(":"))
        {
            String userPassword = url.getAuthority().split("@")[0];
            String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
            con.setRequestProperty("Authorization", "Basic " + encoding);
        }
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        }catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    @Override
    public String getDescription() {
        return urlResource.getDescription() + " with Basic Authorization";
    }
}
