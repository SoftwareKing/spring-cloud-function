/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.function.stream;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.cloud.function.support.FluxSupplier;
import org.springframework.cloud.function.support.FunctionUtils;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;

/**
 * @author Mark Fisher
 */
public class SupplierInvokingMessageProducer<T> extends MessageProducerSupport {

	private final Supplier<Flux<T>> supplier;

	public SupplierInvokingMessageProducer(Supplier<?> supplier, long interval) {
		Assert.notNull(supplier, "Supplier must not be null");
		if (!FunctionUtils.isFluxSupplier(supplier)) {
			supplier = (interval > 0)
					? new FluxSupplier<>(supplier, Duration.ofMillis(interval))
					: new FluxSupplier<>(supplier);
		}
		@SuppressWarnings("unchecked")
		Supplier<Flux<T>> unchecked = (Supplier<Flux<T>>) supplier;
		this.supplier = unchecked;
		this.setOutputChannelName(Source.OUTPUT);
	}

	@Override
	protected void doStart() {
		this.supplier.get()
				.subscribe(m -> this.sendMessage(MessageBuilder.withPayload(m).build()));
	}
}
