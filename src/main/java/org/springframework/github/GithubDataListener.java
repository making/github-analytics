/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.github;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.analytics.metrics.FieldValueCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GithubDataListener {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	@Autowired private FieldValueCounterRepository fieldValueCounterRepository;
	Map<String, Object> counter = new HashMap<>();
	AtomicInteger stats = new AtomicInteger();

	@StreamListener(Sink.INPUT)
	public void listen(GithubData data) {
		log.info("Received a new message [" + data + "]");
		processValue("repository", data.getRepository());
		processValue("username", data.getUsername());
		processValue("type", data.getType());
		processValue("action", data.getAction());
		stats.incrementAndGet();
	}

	@RequestMapping(value = "/data", method = RequestMethod.POST)
	public void data(GithubData data) {
		log.info("Received a new request [" + data + "]");
		listen(data);
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	public int count() {
		int size = this.stats.get();
		log.info("Size of counters equals [" + size + "]");
		return size;
	}

	void clear() {
		counter.clear();
	}

	public void processValue(String counterName, Object value) {
		if ((value instanceof Collection) || ObjectUtils.isArray(value)) {
			Collection<?> c = (value instanceof Collection) ? (Collection<?>) value
					: Arrays.asList(ObjectUtils.toObjectArray(value));
			for (Object val : c) {
				this.fieldValueCounterRepository.increment(counterName, val.toString(), 1.0);
			}
		}
		else if (value != null) {
			this.fieldValueCounterRepository.increment(counterName, value.toString(), 1.0);
		}
		counter.put(counterName, value);
	}

}
