package org.springframework.github;

import io.codearte.accurest.stubrunner.StubTrigger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AnalyticsApplication.class)
public class AnalyticsApplicationTests {

	@Autowired
	private Sink sink;

	@Autowired StubTrigger stubTriggerer;

	@Test
	public void testWithMarshalledPojo() throws JsonProcessingException {
		GithubData data = new GithubData();
		data.setRepository("spring-framework");
		data.setUsername("rossen");
		String json = new ObjectMapper().writeValueAsString(data);
		Message<String> message =
				MessageBuilder.withPayload(json).setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON).build();
		this.sink.input().send(message);
	}

	@Test
	public void testWithV1StubData() {
		this.stubTriggerer.trigger("issue_created_v1");
	}

	@Test
	public void testWithV2StubData() {
		this.stubTriggerer.trigger("issue_created_v2");
	}

}
