/*
  Copyright (c) 2015, Alcatel-Lucent Inc
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the copyright holder nor the names of its contributors
        may be used to endorse or promote products derived from this software without
        specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.github.nuagenetworks.bambou;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nuagenetworks.bambou.spring.TestSpringConfig;
import com.github.nuagenetworks.bambou.testobj.TestChildObject;
import com.github.nuagenetworks.bambou.testobj.TestChildTemplateObject;
import com.github.nuagenetworks.bambou.testobj.TestObject;
import com.github.nuagenetworks.bambou.testobj.TestRootObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSpringConfig.class, loader = AnnotationConfigContextLoader.class)
public class RestObjectTest {

	@Autowired
	private RestOperations restOperations;

	@Autowired
	private RestSession<TestRootObject> session;

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testNewObject() {
		String id = "12345";
		String parentId = "67890";
		String parentType = "parentType";
		String creationDate = "2016-03-21";
		String lastUpdatedDate = "2016-03-22";
		String owner = "martin";

		RestObject object = new RestObject();
		object.setId(id);
		object.setParentId(parentId);
		object.setParentType(parentType);
		object.setCreationDate(creationDate);
		object.setLastUpdatedDate(lastUpdatedDate);
		object.setOwner(owner);

		Assert.assertEquals(id, object.getId());
		Assert.assertEquals(parentId, object.getParentId());
		Assert.assertEquals(parentType, object.getParentType());
		Assert.assertEquals(creationDate, object.getCreationDate());
		Assert.assertEquals(lastUpdatedDate, object.getLastUpdatedDate());
		Assert.assertEquals(owner, object.getOwner());
	}

	@Test
	public void testFetchObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create response object to fetch REST call
		TestObject refObject = new TestObject();
		refObject.setId(id);
		refObject.setMyProperty("MyValue");

		// Start session
		startSession(restOperations, "object/" + id, HttpMethod.GET, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(refObject)));

		// Fetch object
		TestObject object = new TestObject();
		object.setId(id);
		object.fetch();

		// Expect some object properties to be set
		Assert.assertEquals(id, object.getId());
		Assert.assertEquals(refObject.getMyProperty(), object.getMyProperty());

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testSaveObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);
		object.setMyProperty("MyProperty");

		// Start session
		startSession(restOperations, "object/" + id, HttpMethod.PUT, HttpStatus.NO_CONTENT, "[]");

		// Save object
		object.save();

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testDeleteObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Start session
		startSession(restOperations, "object/" + id + "?responseChoice=1", HttpMethod.DELETE, HttpStatus.NO_CONTENT, "[]");

		// Delete object
		object.delete();

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testCreateChildObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child object
		TestChildObject childObject = new TestChildObject();
		childObject.setMyOtherProperty("MyOtherValue");

		// Start session
		startSession(restOperations, "object/" + id + "/childobject", HttpMethod.POST, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(childObject)));

		// Create child
		object.createChild(childObject);

		// Expect object's fetcher to contain the new child object
		Assert.assertEquals(1, object.getChildObjectFetcher().size());
		Assert.assertTrue(object.getChildObjectFetcher().get(0) == childObject);

		// Verify mock calls
		EasyMock.verify(restOperations);

	}

	@Test
	public void testInstantiateChildObject() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child template object
		TestChildTemplateObject childTemplate = new TestChildTemplateObject();
		childTemplate.setId("67890");

		// Create child object
		TestChildObject childObject = new TestChildObject();

		// Start session
		startSession(restOperations, "object/" + id + "/childobject", HttpMethod.POST, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(childObject)));

		// Instantiate child
		object.instantiateChild(childObject, childTemplate);

		// Expect template id to be set
		Assert.assertEquals(childTemplate.getId(), childObject.getTemplateId());

		// Expect object's fetcher to contain the new child object
		Assert.assertEquals(1, object.getChildObjectFetcher().size());
		Assert.assertTrue(object.getChildObjectFetcher().get(0) == childObject);

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	@Test
	public void testAssignObjects() throws RestException, RestClientException, JsonProcessingException {
		String id = "12345";

		// Create object
		TestObject object = new TestObject();
		object.setId(id);

		// Create child objects
		List<RestObject> childObjects = new ArrayList<RestObject>();
		TestChildObject childObject1 = new TestChildObject();
		childObject1.setId("1");
		childObjects.add(childObject1);
		TestChildObject childObject2 = new TestChildObject();
		childObject2.setId("2");
		childObjects.add(childObject2);
		TestChildObject childObject3 = new TestChildObject();
		childObject3.setId("3");
		childObjects.add(childObject3);

		// Start session
		startSession(restOperations, "object/" + id + "/childobject", HttpMethod.PUT, HttpStatus.OK, mapper.writeValueAsString(Arrays.asList(object)));

		// Assign child objects
		object.assign(childObjects);

		// Verify mock calls
		EasyMock.verify(restOperations);
	}

	private RestSession<TestRootObject> startSession(RestOperations restOperations, String urlSuffix, HttpMethod method, HttpStatus responseStatus,
	        String responseString) throws RestException {
		String username = "martin";
		String password = "martin";
		String enterprise = "martin";
		String apiUrl = "http://vsd";
		String apiPrefix = "api";
		double version = 2.1;

		Capture<HttpEntity<?>> capturedHttpEntity = EasyMock.newCapture();

		// Expected REST calls
		EasyMock.reset(restOperations);
		EasyMock.expect(restOperations.exchange(EasyMock.eq(apiUrl + '/' + apiPrefix + "/v2_1/root"), EasyMock.eq(HttpMethod.GET),
		        EasyMock.anyObject(HttpEntity.class), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>("[{}]", HttpStatus.OK));
		EasyMock.expect(restOperations.exchange(EasyMock.eq(apiUrl + '/' + apiPrefix + "/v2_1/" + urlSuffix), EasyMock.eq(method),
		        EasyMock.capture(capturedHttpEntity), EasyMock.eq(String.class))).andReturn(new ResponseEntity<String>(responseString, responseStatus));
		EasyMock.replay(restOperations);

		// Start REST session
		session.setUsername(username);
		session.setPassword(password);
		session.setEnterprise(enterprise);
		session.setApiUrl(apiUrl);
		session.setApiPrefix(apiPrefix);
		session.setVersion(version);
		session.start();

		return session;
	}
}
