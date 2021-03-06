package integration;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTests.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
public class IntegrationTests {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	@Value("${stubrunner.url}") String stubRunnerUrl;
	@Value("${application.url}") String applicationUrl;

	TestRestTemplate testRestTemplate = new TestRestTemplate();

	@Test
	public void shouldStoreAMessageWhenGithubDataWasReceived() {
		final Integer countOfEntries = countGithubData();
		log.info("Initial count is [" + countOfEntries + "]");
		ResponseEntity<Map> response = triggerMessage();
		then(response.getStatusCode().is2xxSuccessful()).isTrue();
		log.info("Triggered additional message");

		log.info("Awaiting proper count of github data");
		await().until(() -> countGithubData() > countOfEntries);
	}

	private ResponseEntity<Map> triggerMessage() {
		return this.testRestTemplate.postForEntity("http://" +
				this.stubRunnerUrl + "/triggers/hook_created_v2", "", Map.class);
	}

	private Integer countGithubData() {
		Integer response = this.testRestTemplate
				.getForObject("http://" + this.applicationUrl + "/count", Integer.class);
		log.info("Received response [" + response + "]");
		return response;
	}
}
