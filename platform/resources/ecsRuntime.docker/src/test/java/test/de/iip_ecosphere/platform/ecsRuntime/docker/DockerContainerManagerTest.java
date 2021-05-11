/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager.FactoryDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Template test.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManagerTest {
        
    /**
     * Template test.
     * @throws URISyntaxException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testContainerManager() throws URISyntaxException, ExecutionException, InterruptedException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE); // no AAS here
        // TODO test against full AAS setup, see EcsAasTest
        DockerContainerManager cm = (DockerContainerManager) EcsFactory.getContainerManager();
        Assert.assertTrue(cm instanceof DockerContainerManager);
        // TODO go on testing with cm
        String testId = "01";
        String testName = "test-container";
        
        //---- Adding container -----------------
        String workingDir = System.getProperty("user.dir");
        String imageLocationStr = workingDir + "/src/test/resources/";
        URI location = new URI(imageLocationStr);

        // Is the id of the container same as in the yaml file?
        Assert.assertEquals(testId, cm.addContainer(location));
        Thread.sleep(2000);
        // Does the container have a Docker Id?
        Assert.assertNotNull(cm.getDockerId(testName));

        /*
        System.out.println("con nach Name: " 
            + dockerClient.listContainersCmd().withNameFilter(Arrays.asList("/test-container")).exec());
        
        System.out.println("con Created: " 
            + dockerClient.listContainersCmd()
                          .withStatusFilter(statusCreatedList)
                          .withNameFilter(Arrays.asList(testName))
                          .exec());
        ArrayList<Container> containers = (ArrayList<Container>) dockerClient.listContainersCmd()
                .withStatusFilter(statusCreatedList)
                .withNameFilter(Arrays.asList(testName))
                .exec();
        System.out.println("Container: " + containers);
        for (int i = 0; i < containers.size(); i++) {
            Container container = containers.get(i);
            String[] dockerNames = container.getNames();
            String dockerName = container.getNames()[0];
            dockerName = dockerName.substring(1, dockerName.length());
            if (dockerName.equals(testName)) {
                String dockerId = container.getId();
                System.out.println("Name: " + dockerName + "  Id"  + dockerId);
            }
            
            
        }
        */
        // TODO Is Docker container with a given name deployed?
        //Assert.assertEquals("Created", getDockerState(dockerId)); does not work
        
        //---- Starting container -----------------
        
        cm.startContainer(testId);
        System.out.println("con started");
        Thread.sleep(3000);
        // Checking if there is a running container with a given name
        Assert.assertNotNull(getContainerId(testName, "running", cm));

        //---- Stopping container -----------------
        
        cm.stopContainer(testId);
        Thread.sleep(3000);
        Assert.assertNull(getContainerId(testName, "running", cm));
        /*
        Thread.sleep(3000);
        Assert.assertEquals("Exited", getDockerState(dockerId));
        */
        // Removing container
        cm.undeployContainer(testId);
        /*
        // Removing container directly with API client
        dockerClient.removeContainerCmd("test-container").exec();
        */
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * todo.
     * @param name
     * @param state
     * @param cm
     * @return Docker container id
     */
    public static String getContainerId(String name, String state, DockerContainerManager cm) {
        DockerClient dockerClient = cm.getDockerClient();
        ArrayList<Container> containers = (ArrayList<Container>) dockerClient.listContainersCmd()
                .withStatusFilter(Arrays.asList(state))
                .withNameFilter(Arrays.asList(name))
                .exec();
        
        if (containers.size() == 0) {
            return null;
        } 
        
        for (int i = 0; i < containers.size(); i++) {
            Container container = containers.get(i);
            String dockerName = container.getNames()[0];
            // removing the slash symbol before the name
            dockerName = dockerName.substring(1, dockerName.length());
            if (dockerName.equals(name)) {
                return container.getId();
            }
        }
        return null;
    }
    /**
     * Returns a Docker state of a given container {@code dockerId}.
     * 
     * @param dockerId Docker id of the container
     * @return state Docker state of the container
     * @throws URISyntaxException
     */
    /*
    public static String getDockerState(String dockerId) throws URISyntaxException {
        String dockerState = null;
        
        Runtime rt = Runtime.getRuntime();
        String command = "docker container ls -a";
        try {
            Process proc = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(proc.getInputStream()));
            
            // Read the output from the command
            String line = null;
            while (true) {
                line = stdInput.readLine();
                if (line == null) {
                    break;
                }
                
                // Output to parse:
                // CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS    
                // 8f6983acd81a        arvindr226/alpine-ssh    "/usr/sbin/sshd -D"      3 weeks ago         Up 3 secon
                
                // Skipping the header
                if (line.substring(0, 12).equals("CONTAINER ID")) {
                    continue;
                }
                String conId = line.substring(0, 12).trim();
                
                if (conId.equals(dockerId)) {
                    String status = line.substring(90, 120).trim();
                    String[] statusAsList = status.split(" ");
                    dockerState = statusAsList[0];
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        
        return dockerState;
    }
    */
}
