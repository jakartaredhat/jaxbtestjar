/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package container;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static java.util.Collections.enumeration;
import static org.jboss.shrinkwrap.api.Filters.include;

/**
 * Hacky class loader to load classes from the /WEB-INF/lib/jakarta.xml.bind-api.jar contents
 */
public class WebArchiveClassLoader extends ShrinkWrapClassLoader {
    private static final URL CONTEXT;
    private static final String ARCHIVE = "archive";
    /**
     * Web specification resource root
     */
    private static final String WAR_ROOT_LOCATION = "/WEB-INF/classes";
    static {
        try {
            CONTEXT = new URL(ARCHIVE,"",0,"",new ArchiveURLStreamHandler(null));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final Archive<?> archive;
    WebArchiveClassLoader(ClassLoader parent, Archive<?> archive) {
        super(parent, archive);
        URL jaxbJarUrl = super.findResource("/WEB-INF/lib/jakarta.xml.bind-api.jar");
        super.addURL(jaxbJarUrl);
        this.archive = archive;
        System.out.printf("WebArchiveClassLoader, archive:%s\n", archive.toString(true));
    }

    @Override
    public URL findResource(String name) {
        final String adjustedName = adjust(name);
        return super.findResource(adjustedName);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        // first read from /WEB-INF/classes
        final String adjustedName = adjust(name);
        final Enumeration<URL> urls = super.findResources(adjustedName);
        final List<URL> collectedUrls = new ArrayList<>();
        while(urls.hasMoreElements()) {
            collectedUrls.add(urls.nextElement());
        }
        WebArchive war = archive.as(WebArchive.class);
        // we need to also check /WEB-INF/lib
        Map<ArchivePath, Node> libs = war.getContent(include("/WEB-INF/lib/.*"));
        for(Map.Entry<ArchivePath, Node> entry : libs.entrySet()) {
            Asset asset = entry.getValue().getAsset();
            if (asset instanceof ArchiveAsset && ((ArchiveAsset) asset).getArchive() instanceof JavaArchive) {
                JavaArchive jar = (JavaArchive)((ArchiveAsset) asset).getArchive();
                if(jar.get(name) != null) {
                    collectedUrls.add(new URL(CONTEXT,ARCHIVE+":"+name,new ArchiveURLStreamHandler(jar)));
                }
            }
        }
        return enumeration(collectedUrls);
    }

    @Override
    public void close() throws IOException {
        super.close();

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        WebArchive war = archive.as(WebArchive.class);
        Node jarNode = war.get("/WEB-INF/lib/jakarta.xml.bind-api.jar");
        FileAsset jarAsset = (FileAsset) jarNode.getAsset();
        try {
            JarFile jarFile = new JarFile(jarAsset.getSource());
            ZipEntry classEntry = jarFile.getEntry(name.replace('.', '/')+".class");
            try (InputStream is = jarFile.getInputStream(classEntry)) {
                byte[] classBytes = is.readAllBytes();
                Class<?> clazz = super.defineClass(name, classBytes, 0, classBytes.length);
                return clazz;
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }

        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private String adjust(String name) {
        if(name.startsWith("/")) {
            return WAR_ROOT_LOCATION + name;
        } else {
            return WAR_ROOT_LOCATION + "/" + name;
        }
    }

    private static class ArchiveURLStreamHandler extends URLStreamHandler {
        private final Archive<?> archive;

        private ArchiveURLStreamHandler(Archive<?> archive) {
            this.archive = archive;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new ArchiveURLConnection(u, archive);
        }
    }

    private static class ArchiveURLConnection extends URLConnection {

        private final Archive<?> archive;

        ArchiveURLConnection(URL url, Archive<?> archive) {
            super(url);
            this.archive = archive;
        }

        @Override
        public void connect() throws IOException {

        }

        @Override
        public InputStream getInputStream() throws IOException {
            String path = super.url.getPath();
            if(archive.get(path) == null || archive.get(path).getAsset() == null) {
                return null;
            }
            return archive.get(path).getAsset().openStream();
        }
    }
}
