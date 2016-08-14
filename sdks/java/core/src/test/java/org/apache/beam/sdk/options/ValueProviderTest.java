/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.beam.sdk.options.ValueProvider.RuntimeValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;

import com.google.common.collect.ImmutableMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ValueProvider}. */
@RunWith(JUnit4.class)
public class ValueProviderTest {
  @Rule public ExpectedException expectedException = ExpectedException.none();

  /** A test interface. */
  public static interface TestOptions extends PipelineOptions {
    @Default.String("bar")
    ValueProvider<String> getBar();
    void setBar(ValueProvider<String> bar);

    ValueProvider<String> getFoo();
    void setFoo(ValueProvider<String> foo);
  }

  @Test
  public void testStaticValueProvider() {
    ValueProvider<String> provider = StaticValueProvider.of("foo");
    assertEquals("foo", provider.get());
    assertTrue(provider.shouldValidate());
  }

  @Test
  public void testNoDefaultRuntimeProvider() {
    TestOptions options = PipelineOptionsFactory.as(TestOptions.class);
    ValueProvider<String> provider = options.getFoo();
    assertFalse(provider.shouldValidate());
  }

  @Test
  public void testDefaultRuntimeProvider() {
    TestOptions options = PipelineOptionsFactory.as(TestOptions.class);
    ValueProvider<String> provider = options.getBar();
    assertTrue(provider.shouldValidate());
    assertEquals("bar", provider.get());
  }

  @Test
  public void testNoDefaultRuntimeProviderWithOverride() {
    ProxyInvocationHandler handler = new ProxyInvocationHandler(
      ImmutableMap.<String, Object>builder().put("bar", "baz").build());
    TestOptions proxy = handler.as(TestOptions.class);
    RuntimeValueProvider.setRuntimeOptions(proxy);

    TestOptions options = PipelineOptionsFactory.as(TestOptions.class);
    ValueProvider<String> provider = options.getFoo();
    assertFalse(provider.shouldValidate());
    assertEquals("baz", provider.get());
  }

  @Test
  public void testDefaultRuntimeProviderWithOverride() {
    ProxyInvocationHandler handler = new ProxyInvocationHandler(
      ImmutableMap.<String, Object>builder().put("bar", "baz").build());
    TestOptions proxy = handler.as(TestOptions.class);
    RuntimeValueProvider.setRuntimeOptions(proxy);

    TestOptions options = PipelineOptionsFactory.as(TestOptions.class);
    ValueProvider<String> provider = options.getBar();
    assertTrue(provider.shouldValidate());
    assertEquals("baz", provider.get());
  }
}
