/*
 * Copyright 2012-2026 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.impl.test;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.authzforce.core.pdp.impl.ResourceLocationResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class ResourceLocationResolverTest
{
	@Test
	public void resolveClasspathResource() throws Exception
	{
		final URL resource = ResourceLocationResolver.getUrl("classpath:logback.xml");
		Assert.assertNotNull(resource);
	}

	@Test
	public void resolveUrl() throws Exception
	{
		Assert.assertEquals("https", ResourceLocationResolver.getUrl("https://example.org/resource").getProtocol());
	}

	@Test
	public void resolveFilePath() throws Exception
	{
		final File file = ResourceLocationResolver.getFile("relative/path.xml");
		Assert.assertEquals(new File("relative/path.xml"), file);
		Assert.assertEquals("file", ResourceLocationResolver.getUrl("relative/path.xml").getProtocol());
	}

	@Test(expected = FileNotFoundException.class)
	public void rejectNonFileUrlAsFile() throws Exception
	{
		ResourceLocationResolver.getFile("https://example.org/resource");
	}

	@Test(expected = FileNotFoundException.class)
	public void rejectMissingClasspathResource() throws Exception
	{
		ResourceLocationResolver.getUrl("classpath:does/not/exist");
	}
}
