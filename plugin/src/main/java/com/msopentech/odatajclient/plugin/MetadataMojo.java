/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.msopentech.odatajclient.plugin;

import com.msopentech.odatajclient.engine.client.http.AbstractBasicAuthHttpClientFactory;
import com.msopentech.odatajclient.engine.communication.request.retrieve.ODataMetadataRequest;
import com.msopentech.odatajclient.engine.communication.request.retrieve.ODataRetrieveRequestFactory;
import com.msopentech.odatajclient.engine.communication.response.ODataRetrieveResponse;
import com.msopentech.odatajclient.engine.data.metadata.EdmMetadata;
import com.msopentech.odatajclient.engine.data.metadata.edm.ComplexType;
import com.msopentech.odatajclient.engine.data.metadata.edm.EntityContainer;
import com.msopentech.odatajclient.engine.data.metadata.edm.EntitySet;
import com.msopentech.odatajclient.engine.data.metadata.edm.EntityType;
import com.msopentech.odatajclient.engine.data.metadata.edm.Schema;
import com.msopentech.odatajclient.engine.utils.Configuration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * POJOs generator.
 */
@Mojo(name = "pojos", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MetadataMojo extends AbstractMojo {

    /**
     * Generated files base root.
     */
    @Parameter(property = "outputDirectory", required = true)
    private String outputDirectory;

    /**
     * OData service root URL.
     */
    @Parameter(property = "serviceRootURL", required = true)
    private String serviceRootURL;
    
    /**
     * OData service username
     */
    @Parameter(property = "serviceUsername")
    private String serviceUsername;
    
    /**
     * OData service password
     */
    @Parameter(property = "servicePassword")
    private String servicePassword;

    /**
     * Base package.
     */
    @Parameter(property = "basePackage", required = true)
    private String basePackage;

    private Utility utility = null;

    private final Set<String> namespaces = new HashSet<String>();

    private static String TOOL_DIR = "ojc-plugin";
    
    private class BasicAccessAuthentication extends AbstractBasicAuthHttpClientFactory {
        
        private final String username;
        private final String password;

        public BasicAccessAuthentication(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String getUsername() {
            return this.username;
        }

        @Override
        protected String getPassword() {
            return this.password;
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (new File(outputDirectory + File.separator + TOOL_DIR).exists()) {
            getLog().info("Nothing to do because " + TOOL_DIR + " directory already exists. Clean to update.");
            return;
        }

        try {
            Velocity.addProperty(Velocity.RESOURCE_LOADER, "class");
            Velocity.addProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    
            determineAndSetBasicAuthentication();

            final ODataMetadataRequest req = ODataRetrieveRequestFactory.getMetadataRequest(serviceRootURL);

            final ODataRetrieveResponse<EdmMetadata> res = req.execute();
            final EdmMetadata metadata = res.getBody();

            if (metadata == null) {
                throw new IllegalStateException("Metadata not found");
            }

            for (Schema schema : metadata.getSchemas()) {
                namespaces.add(schema.getNamespace().toLowerCase());
            }

            final Set<String> complexTypeNames = new HashSet<String>();
            final File services = mkdir("META-INF/services");

            for (Schema schema : metadata.getSchemas()) {
                utility = new Utility(metadata, schema, basePackage);

                // write package-info for the base package
                final String schemaPath = utility.getNamespace().toLowerCase().replace('.', File.separatorChar);
                final File base = mkPkgDir(schemaPath);
                final String pkg = basePackage + "." + utility.getNamespace().toLowerCase();
                parseObj(base, pkg, "package-info", "package-info.java");

                // write package-info for types package
                final File typesBaseDir = mkPkgDir(schemaPath + "/types");
                final String typesPkg = pkg + ".types";
                parseObj(typesBaseDir, typesPkg, "package-info", "package-info.java");

                final Map<String, Object> objs = new HashMap<String, Object>();

                // write types into types package
                for (ComplexType complex : schema.getComplexTypes()) {
                    final String className = utility.capitalize(complex.getName());
                    complexTypeNames.add(typesPkg + "." + className);
                    objs.clear();
                    objs.put("complexType", complex);
                    parseObj(typesBaseDir, typesPkg, "complexType", className + ".java", objs);
                }

                for (EntityType entity : schema.getEntityTypes()) {
                    objs.clear();
                    objs.put("entityType", entity);

                    final Map<String, String> keys;

                    EntityType baseType = null;
                    if (entity.getBaseType() == null) {
                        keys = utility.getEntityKeyType(entity);
                    } else {
                        baseType = schema.getEntityType(utility.getNameFromNS(entity.getBaseType()));
                        objs.put("baseType", utility.getJavaType(entity.getBaseType()));
                        while (baseType.getBaseType() != null) {
                            baseType = schema.getEntityType(utility.getNameFromNS(baseType.getBaseType()));
                        }
                        keys = utility.getEntityKeyType(baseType);
                    }

                    if (keys.size() > 1) {
                        // create compound key class
                        final String keyClassName = utility.capitalize(baseType == null
                                ? entity.getName()
                                : baseType.getName()) + "Key";
                        objs.put("keyRef", keyClassName);

                        if (entity.getBaseType() == null) {
                            objs.put("keys", keys);
                            parseObj(typesBaseDir, typesPkg, "entityTypeKey", keyClassName + ".java", objs);
                        }
                    }

                    parseObj(typesBaseDir, typesPkg, "entityType",
                            utility.capitalize(entity.getName()) + ".java", objs);
                    parseObj(typesBaseDir, typesPkg, "entityCollection",
                            utility.capitalize(entity.getName()) + "Collection.java", objs);
                }

                // write container and top entity sets into the base package
                for (EntityContainer container : schema.getEntityContainers()) {
                    objs.clear();
                    objs.put("container", container);
                    parseObj(base, pkg, "container",
                            utility.capitalize(container.getName()) + ".java", objs);

                    for (EntitySet entitySet : container.getEntitySets()) {
                        objs.clear();
                        objs.put("entitySet", entitySet);
                        parseObj(base, pkg, "entitySet",
                                utility.capitalize(entitySet.getName()) + ".java", objs);
                    }
                }

                parseObj(services, true, null, "services", "com.msopentech.odatajclient.proxy.api.AbstractComplexType",
                        Collections.singletonMap("services", (Object) complexTypeNames));
            }
        } catch (Throwable t) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            t.printStackTrace(printWriter);
            getLog().error(stringWriter.toString());

            throw (t instanceof MojoExecutionException)
                    ? (MojoExecutionException) t
                    : new MojoExecutionException("While executin mojo", t);
        }
    }

    private void determineAndSetBasicAuthentication() {
        if (this.serviceUsername != null) {
            Configuration.setHttpClientFactory(new BasicAccessAuthentication(this.serviceUsername, this.servicePassword));
        }
    }

    private File mkdir(final String path) {
        final File dir = new File(outputDirectory + File.separator + TOOL_DIR + File.separator + path);

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Invalid path '" + path + "': it is not a directory");
            }
        } else {
            dir.mkdirs();
        }

        return dir;
    }

    private File mkPkgDir(final String path) {
        return mkdir(basePackage.replace('.', File.separatorChar) + File.separator + path);
    }

    private void writeFile(final String name, final File path, final VelocityContext ctx, final Template template,
            final boolean append) throws MojoExecutionException {

        if (!path.exists()) {
            throw new IllegalArgumentException("Invalid base path '" + path.getAbsolutePath() + "'");
        }

        FileWriter writer = null;
        try {
            final File toBeWritten = new File(path, name);
            if (!append && toBeWritten.exists()) {
                throw new IllegalStateException("File '" + toBeWritten.getAbsolutePath() + "' already exists");
            }
            writer = new FileWriter(toBeWritten, append);
            template.merge(ctx, writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file '" + name + "'", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private VelocityContext newContext() {

        final VelocityContext ctx = new VelocityContext();

        ctx.put("utility", utility);
        ctx.put("basePackage", basePackage);
        ctx.put("schemaName", utility.getSchemaName());
        ctx.put("namespace", utility.getNamespace());
        ctx.put("namespaces", namespaces);

        return ctx;
    }

    private void parseObj(final File base, final String pkg, final String name, final String out)
            throws MojoExecutionException {

        parseObj(base, false, pkg, name, out, Collections.<String, Object>emptyMap());
    }

    private void parseObj(
            final File base,
            final String pkg,
            final String name,
            final String out,
            final Map<String, Object> objs)
            throws MojoExecutionException {

        parseObj(base, false, pkg, name, out, objs);
    }

    private void parseObj(
            final File base,
            final boolean append,
            final String pkg,
            final String name,
            final String out,
            final Map<String, Object> objs)
            throws MojoExecutionException {

        final VelocityContext ctx = newContext();
        ctx.put("package", pkg);

        if (objs != null) {
            for (Map.Entry<String, Object> obj : objs.entrySet()) {
                if (StringUtils.isNotBlank(obj.getKey()) && obj.getValue() != null) {
                    ctx.put(obj.getKey(), obj.getValue());
                }
            }
        }

        final Template template = Velocity.getTemplate(name + ".vm");
        writeFile(out, base, ctx, template, append);
    }
}
