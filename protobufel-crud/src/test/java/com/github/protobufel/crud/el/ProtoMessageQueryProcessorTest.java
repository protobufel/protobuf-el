//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of the <organization> nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

package com.github.protobufel.crud.el;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ELProcessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fictional.test.GalaxyProto.City;
import com.fictional.test.GalaxyProto.Galaxy;
import com.fictional.test.GalaxyProto.Galaxy.Color;
import com.fictional.test.GalaxyProto.Galaxy.Star;
import com.github.protobufel.DynamicMessage;
import com.github.protobufel.IDynamicMessageProvider;
import com.github.protobufel.el.ProtoELProcessorEx;
import com.github.protobufel.test.util.ProtoUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.Message;

@RunWith(JUnit4.class)
public class ProtoMessageQueryProcessorTest {
  private static final Logger log = LoggerFactory.getLogger(ProtoMessageQueryProcessorTest.class);
  private static final int SEARCH_LIST_SIZE = 5;
  @SuppressWarnings("unused")
  private ELProcessor protoElp;
  private Galaxy originalMsg;
  private List<Message> originalCityList;
  @SuppressWarnings("unused")
  private List<Message> originalGalaxyList;
  private IDynamicMessageProvider messageProvider;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    messageProvider = DynamicMessage.getProvider();
    originalMsg = ProtoUtils.newGalaxy();
    protoElp = new ProtoELProcessorEx();
    originalCityList = newCitySearchList(SEARCH_LIST_SIZE);
    originalGalaxyList = newGalaxySearchList(SEARCH_LIST_SIZE);
  }

  private List<Message> newGalaxySearchList(final int size) {
    final List<Message> list = Lists.newArrayList();

    for (int i = 0; i < size; i++) {
      final Galaxy.Builder builder = originalMsg.toBuilder();
      final Message msg =
          builder.addCode(i).addKeyword(Integer.toString(i))
              .addStar(Star.newBuilder(builder.getStar(0)).setName("Star " + Integer.toString(i)))
              .build();
      list.add(msg);
    }

    return list;
  }

  private List<Message> newCitySearchList(final int size) {
    final List<Message> list = Lists.newArrayList();
    final City city = originalMsg.getStar(0).getPlanet(0).getCountry(0).getCity(0);

    for (int i = 0; i < size; i++) {
      final City.Builder builder = City.newBuilder(city);
      final Message msg =
          builder.addCode(i).addKeyword(Integer.toString(i)).setName("City " + Integer.toString(i))
              .build();
      list.add(msg);
    }

    return list;
  }

  @Test
  public void testIdentityProcess() throws Exception {
    final List<Message> actual =
        ProtoMessageQueryProcessor.getIdentityProcessor().process(originalCityList);
    assertThat(actual, equalTo(originalCityList));
  }

  @Test
  public void testEmptyProcess() throws Exception {
    final List<Message> actual =
        ProtoMessageQueryProcessor.getEmptyProcessor().process(originalCityList);
    assertThat(actual, equalTo(Collections.<Message>emptyList()));
  }

  @Test
  public void testDeleteRecordProcess() throws Exception {
    final List<Message> expected = new ArrayList<Message>(originalCityList);
    expected.remove(0);
    final List<Message> actual =
        ProtoMessageQueryProcessor.builder().setMessageProvider(messageProvider)
            .setExpression("(index==0) ? null : record").process(originalCityList);
    assertThat(actual, equalTo(expected));
  }

  @Test
  public void testAddRecordProcess() throws Exception {
    final List<Message> expected = new ArrayList<Message>(originalCityList);
    expected.add(0, expected.get(0));
    final List<Message> actual =
        ProtoMessageQueryProcessor.builder().setMessageProvider(messageProvider)
            .setExpression("(index==0) ? (msg=record.build(); [msg, msg]) : record")
            .process(originalCityList);
    assertThat(actual, equalTo(expected));
  }

  @Test
  public void testValidationMessageTypeProcess() throws IllegalArgumentException {
    final Map<String, Object> beans =
        ImmutableMap.<String, Object>of("wrongResults", newGalaxySearchList(SEARCH_LIST_SIZE));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("record #0 is of the wrong message type"));
    ProtoMessageQueryProcessor.builder().setMessageProvider(messageProvider)
        .setExpression("wrongResults[index]").process(originalCityList, beans);
  }

  @Test
  public void testValidationNotMessageOrBuilderProcess() throws IllegalArgumentException {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(containsString("record #0 is not a MessageOrBuilder"));
    ProtoMessageQueryProcessor.builder().setMessageProvider(messageProvider)
        .setExpression("[1, 2]").process(originalCityList);
  }

  @Test
  public void testMutateRecordWithEnumProcess() throws Exception {
    final List<Message> expected = new ArrayList<Message>(originalCityList);
    expected.set(0, ((City.Builder) expected.get(0).toBuilder()).setColor(Color.RED).build());
    final List<Message> actual =
        ProtoMessageQueryProcessor.builder().setMessageProvider(messageProvider)
            .setExpression("(index==0) ? (record.color='RED') : null; record")
            .process(originalCityList);
    assertThat(actual, equalTo(expected));
  }
}
