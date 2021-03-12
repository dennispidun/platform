/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.spring;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.spring.descriptor.Validator;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.services.spring.yaml.YamlEndpoint;
import de.iip_ecosphere.platform.services.spring.yaml.YamlProcess;
import de.iip_ecosphere.platform.services.spring.yaml.YamlRelation;
import de.iip_ecosphere.platform.services.spring.yaml.YamlService;
import de.iip_ecosphere.platform.services.spring.yaml.YamlServiceDependency;

/**
 * Tests the YAML descriptor implementation and the {@link Validator}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArtifactInfoTest {

    /**
     * Tests the YAML reader.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        YamlArtifact info = YamlArtifact.readFromYaml(null);
        Assert.assertTrue(info.getServices().isEmpty());
        
        try {
            info = YamlArtifact.readFromYaml(getClass().getClassLoader().getResourceAsStream("test-error.yml"));
            Assert.fail("No exception");
        } catch (IOException e) {
            // ok here, desired
        }
        
        // failing test.yml
        info = YamlArtifact.readFromYaml(getClass().getClassLoader().getResourceAsStream("test.yml"));
        Assert.assertEquals("art", info.getId());
        Assert.assertEquals("art-name", info.getName());
        Assert.assertFalse(info.getServices().isEmpty());
        Assert.assertEquals(2, info.getServices().size());
        
        YamlService service = info.getServices().get(0);
        assertServiceBasics(service, "id-0", "name-0", "1.0.2", "desc desc-0");
        assertServiceCharacteristics(service, true, ServiceKind.SOURCE_SERVICE);
        assertStringList(service.getCmdArg(), "arg-0-1", "arg-0-2");
        assertStringList(service.getEnsembleWith(), "id-1");
        Assert.assertEquals(0, service.getDependencies().size());
        Assert.assertEquals(2, service.getRelations().size());
        service.getRelations().get(0);
        assertRelation(service.getRelations().get(0), "", 1234, "localhost");
        assertRelation(service.getRelations().get(1), "input", 9872, "me.here.de");
        Assert.assertNull(service.getProcess());
        
        service = info.getServices().get(1);
        assertServiceBasics(service, "id-1", "name-1", "1.0.3", "desc desc-1");
        assertServiceCharacteristics(service, true, ServiceKind.SINK_SERVICE);
        assertStringList(service.getCmdArg());
        assertStringList(service.getEnsembleWith());
        Assert.assertEquals(1, service.getDependencies().size());
        assertDependency(service.getDependencies().get(0), "id-0");
        Assert.assertEquals(1, service.getRelations().size());
        assertRelation(service.getRelations().get(0), "output", 9872, "me.here.de");
        Assert.assertNotNull(service.getProcess());
        assertProcess(service.getProcess(), "impl/python", "python", "MyServiceWrapper.py");
        Assert.assertEquals(2, service.getInstances());
        Assert.assertEquals(1024, service.getMemory());
        Assert.assertEquals(500, service.getDisk());
        Assert.assertEquals(2, service.getCpus());
        
        Validator val = new Validator();
        val.validate(info);
        Assert.assertFalse(val.hasMessages());
        val.clear();
    }
    
    /**
     * Tests a structurally correct but invalid YAML file.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testInvalidYaml() throws IOException {
        YamlArtifact info = YamlArtifact.readFromYaml(getClass().getClassLoader()
            .getResourceAsStream("test-invalid.yml"));
        Validator val = new Validator();
        val.validate(info);
        Assert.assertTrue(val.hasMessages());
        System.out.println("> Validation output for test-invalid.yml:");
        System.out.println(val.getMessages());
        System.out.println("< Validation output for test-invalid.yml:");
        val.clear();
    }
    
    /**
     * Asserts a list of strings w.r.t. the {@code expected} values.
     * 
     * @param list the list
     * @param expected the expected values
     */
    private static void assertStringList(List<String> list, String... expected) {
        Assert.assertNotNull(list);
        Assert.assertEquals(expected.length, list.size());
        int a = 0;
        for (String e: expected) {
            Assert.assertEquals(e, list.get(a));
            a++;
        }
    }
    
    /**
     * Asserts basic properties of a {@link YamlService}.
     * 
     * @param service the service instance to be asserted
     * @param id the expected service id
     * @param name the expected service name
     * @param version the expected service version
     * @param descr the expected description
     */
    private static void assertServiceBasics(YamlService service, String id, String name, String version, String descr) {
        Assert.assertNotNull(service);
        Assert.assertEquals(id, service.getId());
        Assert.assertEquals(name, service.getName());
        Assert.assertEquals(version, service.getVersion());
        Assert.assertEquals(descr, service.getDescription());
    }

    /**
     * Asserts additional characteristics of a {@link YamlService}.
     * 
     * @param service the service instance to be asserted
     * @param deployable whether it is expected that the service is deployable
     * @param kind the expected service kind
     */
    private static void assertServiceCharacteristics(YamlService service, boolean deployable, ServiceKind kind) {
        Assert.assertNotNull(service);
        Assert.assertEquals(deployable, service.isDeployable());
        Assert.assertEquals(kind, service.getKind());
    }

    /**
     * Asserts properties of a {@link YamlRelation}.
     * 
     * @param relation the relation to be asserted
     * @param channel the expected channel name/id
     * @param port the port number to be used/substituted
     * @param host the host name to be used/substituted
     */
    private static void assertRelation(YamlRelation relation, String channel, int port, String host) {
        Assert.assertNotNull(relation);
        Assert.assertEquals(channel, relation.getChannel());
        assertEndpoint(relation.getEndpoint(), port, host);
    }

    /**
     * Asserts properties of an {@link YamlEndpoint}.
     * 
     * @param port the port number to be used/substituted
     * @param host the host name to be used/substituted
     * @param endpoint the endpoint to be asserted
     */
    private static void assertEndpoint(YamlEndpoint endpoint, int port, String host) {
        Assert.assertNotNull(endpoint);
        Assert.assertEquals(endpoint.getPortArg().replace(YamlEndpoint.PORT_PLACEHOLDER, String.valueOf(port)), 
            endpoint.getPortArg(port));
        Assert.assertEquals(endpoint.getHostArg().replace(YamlEndpoint.HOST_PLACEHOLDER, host), 
            endpoint.getHostArg(host));
    }
    
    /**
     * Asserts properties of a {@link YamlServiceDependency} (to be extended).
     * 
     * @param id the expected service id
     * @param dependency the dependency to be asserted
     */
    private static void assertDependency(YamlServiceDependency dependency, String id) {
        Assert.assertNotNull(dependency);
        Assert.assertEquals(id, dependency.getId());
    }
    
    /**
     * Asserts {@link YamlProcess} information.
     * 
     * @param process the process to assert
     * @param path the expected path
     * @param cmdArgs the expected command line arguments
     */
    private static void assertProcess(YamlProcess process, String path, 
        String... cmdArgs) {
        Assert.assertEquals(path, process.getPath());
        assertEndpoint(process.getStreamEndpoint(), 1234, "localhost");
        assertEndpoint(process.getAasEndpoint(), 1235, "aas.de");
        assertStringList(process.getCmdArg(), cmdArgs);
    }
    
}