/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.indyrepositorymanager;


import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(ContainerTest.class)
public class ExtraDependencyRepositoriesTest extends AbstractImportTest {

    @Test
    public void shouldExtractReposFromString() {
        Map<String, String> genericParams = createGenericParamsMap("http://central.maven.org/maven2/\n" +
                "https://maven.repository.redhat.com/ga/");

        List<ArtifactRepository> repos = driver.extractExtraRepositoriesFromGenericParameters(genericParams);
        assertEquals(2, repos.size());
        assertTrue("http://central.maven.org/maven2/".equals(repos.get(0).getUrl()));
        assertTrue("central-maven-org".equals(repos.get(0).getId()));
        assertTrue("https://maven.repository.redhat.com/ga/".equals(repos.get(1).getUrl()));
        assertTrue("maven-repository-redhat-com".equals(repos.get(1).getId()));
    }

    @Test
    public void shouldSkipMalformedURL() {
        Map<String, String> genericParams = createGenericParamsMap("http/central.maven.org/maven2/");

        List<ArtifactRepository> repos = driver.extractExtraRepositoriesFromGenericParameters(genericParams);
        assertEquals(0, repos.size());
    }

    @Test
    public void shouldAddExtraRepositoryToBuildGroup() throws RepositoryManagerException, IndyClientException {
        final String REPO_NAME = "i-test-com";
        Map<String, String> genericParams = createGenericParamsMap("http://test.com/maven/");
        BuildExecution execution = new TestBuildExecution();

        RepositorySession repositorySession = driver.createBuildRepository(execution, accessToken, accessToken, RepositoryType.MAVEN, genericParams);
        assertNotNull(repositorySession);

        StoreKey buildGroupKey = new StoreKey(MavenPackageTypeDescriptor.MAVEN_PKG_KEY, StoreType.group, repositorySession.getBuildRepositoryId());
        Group buildGroup = indy.stores().load(buildGroupKey, Group.class);

        Optional<Integer> hits = buildGroup.getConstituents()
                .stream()
                .map((key) -> {
                    if (REPO_NAME.equals(key.getName())) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .reduce((e1, e2) -> (e1 + e2));
        assertTrue(hits.isPresent());
        assertEquals(1, hits.get().intValue());
    }

    private Map<String, String> createGenericParamsMap(String repoString) {
        Map<String, String> genericParams = new HashMap<>();
        genericParams.put(RepositoryManagerDriver.EXTRA_PUBLIC_REPOSITORIES_KEY, repoString);
        return genericParams;
    }
}