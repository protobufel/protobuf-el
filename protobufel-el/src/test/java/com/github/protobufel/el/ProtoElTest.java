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

package com.github.protobufel.el;

import static com.github.protobufel.test.util.ProtoUtils.galaxyKeyword;
import static com.github.protobufel.test.util.ProtoUtils.galaxyStar;
import static com.github.protobufel.test.util.ProtoUtils.planetCountry;
import static com.github.protobufel.test.util.ProtoUtils.starKeyword;
import static com.github.protobufel.test.util.ProtoUtils.starName;
import static com.github.protobufel.test.util.ProtoUtils.starPlanet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.ELProcessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fictional.test.GalaxyProto.Galaxy;
import com.fictional.test.GalaxyProto.Galaxy.Star;
import com.github.protobufel.DynamicMessage;
import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.test.util.ProtoUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

@RunWith(JUnit4.class)
public class ProtoElTest {
  private static final Logger log = LoggerFactory.getLogger(ProtoElTest.class);
  private ELProcessor elp;
  private ELProcessor stdElp;
  private ELProcessor protoElp;
  private Galaxy originalMsg;
  private IBuilder2 builder;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    originalMsg = ProtoUtils.newGalaxy();

    elp = new ELProcessorEx();
    stdElp = new ELProcessor();
    protoElp = new ProtoELProcessorEx();
    builder = DynamicMessage.newBuilder(originalMsg);
  }

  @Test
  public void testEl1() {
    stdElp.defineBean("employee", new Employee("Charlie Brown"));

    final String name = (String) stdElp.eval("employee.name");
    log.debug("name = '{}'", name);

    @SuppressWarnings("unchecked")
    final List<String> keywords =
        (List<String>) stdElp.eval("employee.keywords = ['one', 'two', 'three']");
    log.debug("keywords = '{}'", keywords);

    @SuppressWarnings("unchecked")
    final List<String> keywords2 =
        (List<String>) stdElp.eval("employee.keywords = ['one', 'two', 'three'];"
            + "employee.keywords.stream().filter(e->e!='two').toList()");
    log.debug("keywords = '{}'", keywords2);

    @SuppressWarnings("unchecked")
    final List<String> keywords3 =
        (List<String>) stdElp.eval("employee.keywords = ['one', 'two', 'three'];"
            + "i=0; employee.keywords.stream().filter(e->(i=i+1; i!=3)).toList()");
    log.debug("keywords = '{}'", keywords3);

    @SuppressWarnings("unchecked")
    final List<String> keywords4 =
        (List<String>) stdElp.eval("list = employee.keywords = ['one', 'two', 'three'];"
            + "list[1] = list[1].toUpperCase(); list");
    log.debug("keywords = '{}'", keywords4);

    @SuppressWarnings("unchecked")
    final Map<String, Long> attributes =
        (Map<String, Long>) stdElp
            .eval("map = employee.attributes = {'one':1, 'two':2, 'three':3};"
                + "map.two = map.two * 111; map");
    log.debug("attributes = '{}'", attributes);

    @SuppressWarnings("unchecked")
    final Map<String, Long> attributes2 =
        (Map<String, Long>) stdElp
            .eval("map = employee.attributes = {'one':1, 'two':2, 'three':3};"
                + "map['two'] = map['two'] * 111; map");
    log.debug("attributes = '{}'", attributes2);
  }

  @Test
  public void testElOverloadedMethod1() {
    elp.defineBean("employee", new Employee("Charlie Brown"));

    final String overloaded1 = (String) elp.eval("employee.overloaded1('param1', 99)");
    log.debug("name = '{}'", overloaded1);
    assertThat(overloaded1, is("overloaded1_string"));
  }

  /**
   * This test shows deficiency of Standard BeanElResolver.
   * <p>
   * It doesn't handle overloaded methods, even in the presence of BeanInfo. One of the overloaded
   * methods will be randomly chosen, and so will randomly fail! The BeanELResolverEx should never
   * fail in the presence of BeanInfo!
   */
  @Test
  public void testStandardElOverloadedMethod1() {
    try {
      stdElp.defineBean("employee", new Employee("Charlie Brown"));
      @SuppressWarnings("unused")
      final String overloaded1 = (String) stdElp.eval("employee.overloaded1('param1', 99)");
    } catch (final ELException e) {
      log.debug("stdElp randomly fails on an overloaded method, this is expected!", e);
    }
  }

  @Test
  public void testGetGeneratedProto1() {
    final Galaxy msg = ProtoUtils.newGalaxy();

    elp.defineBean("msg", msg);

    final Star expectedStar1 = msg.getStar(0);
    final Star actualStar1 = (Star) elp.eval("msg.getStar(0)");
    log.debug("actualStar1 = '{}'", actualStar1);
    assertThat(actualStar1, is(expectedStar1));
  }

  @Test
  public void testGetGeneratedProtoDynamicStyle1() throws ELException {
    final Galaxy msg = ProtoUtils.newGalaxy();

    elp.defineBean("msg", msg);

    final Star expectedStar1 = msg.getStar(0);

    expectedException.expect(ELException.class);
    expectedException.expectMessage(containsString(FieldDescriptor.class.getName()));
    final Star actualStar1 = (Star) elp.eval("msg.getField('star')[0]");

    log.debug("actualStar1 = '{}'", actualStar1);
    assertThat(actualStar1, is(expectedStar1));
  }

  @Test
  public void testGetDMProto1() {
    protoElp.defineBean("builder", builder);

    final Message expectedStar1 = originalMsg.getStar(0);
    final Message expectedDMStar1 = (Message) builder.clone().getRepeatedField(galaxyStar, 0);
    assertThat("generated star = DM star", expectedStar1, is(expectedDMStar1));
    final Message actualStar1 = (Message) protoElp.eval("builder.star[0].clone().build()");
    log.debug("actualStar1 = '{}'", actualStar1);
    assertThat("EL star = generated star", actualStar1, is(expectedStar1));
  }

  @Test
  public void testSetDMProto1() {
    protoElp.defineBean("builder", builder);

    final Message expectedMessage =
        builder.clone().getFieldBuilder(galaxyStar, 0).setField(starName, "Star1*").toParent()
            .build();

    final Message actualMessage =
        (Message) protoElp.eval("builder.star[0].name = 'Star1*'; builder.build()");
    log.debug("actualGalaxy = '{}'", actualMessage);
    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void testSetDMProto2() {
    protoElp.defineBean("builder", builder);

    final IBuilder2 expectedBuilder = builder.clone();

    for (final IBuilder2 child : ensureList(expectedBuilder.getBuilderList(galaxyStar))) {
      child.setField(starName, child.getField(starName) + "*");
    }

    final Message expectedMessage = expectedBuilder.build();

    final Message actualMessage =
        (Message) protoElp
            .eval("builder.star.getBuilders().stream().forEach(b->(b.name = b.name += '*')); builder.build()");
    log.debug("actualGalaxy = '{}'", actualMessage);
    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void testSetDMProto3() {
    protoElp.defineBean("builder", builder);
    protoElp.defineBean("log", log);

    final IBuilder2 expectedBuilder = builder.clone();

    for (final IBuilder2 star : ensureList(expectedBuilder.getBuilderList(galaxyStar))) {
      for (final IBuilder2 planet : ensureList(star.getBuilderList(starPlanet))) {
        if (planet.getRepeatedFieldCount(planetCountry) > 0) {
          planet.removeRepeatedField(planetCountry, 0);
        }
      }
    }

    final Message expectedMessage = expectedBuilder.build();

    final Message actualMessage =
        (Message) protoElp.eval("builder.star.getBuilders().stream()"
            + ".flatMap(b->b.planet.getBuilders().stream()).filter(b->(b.country.size() > 0))"
            + ".forEach(b->b.country.remove(0))" + ".toList(); builder.build()");

    log.debug("actualGalaxy = '{}'", actualMessage);
    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void testGetDMBuilderAttributeList1() {
    protoElp.defineBean("builder", builder);
    protoElp.defineBean("log", log);

    final IBuilder2 expectedBuilder = builder.clone();

    @SuppressWarnings("unchecked")
    List<String> expectedList =
        (List<String>) expectedBuilder.getFieldBuilder(galaxyStar, 0).getField(starKeyword);
    expectedList = ImmutableList.<String>builder().addAll(expectedList).add("***1").build();

    @SuppressWarnings("unchecked")
    final List<String> actualList =
        (List<String>) protoElp.getValue("list=builder.star[0].keyword.add('***1').getList()",
            List.class);
    log.debug("builder.star[0].keyword = '{}'", actualList);

    assertThat(actualList, is(expectedList));
  }

  @Test
  public void testGetDMBuilderAttributeList2() {
    protoElp.defineBean("builder", builder);
    protoElp.defineBean("log", log);

    final IBuilder2 expectedBuilder = builder.clone();

    @SuppressWarnings("unchecked")
    List<String> expectedList = (List<String>) expectedBuilder.getField(galaxyKeyword);
    expectedList = ImmutableList.<String>builder().addAll(expectedList).add("***1").build();

    @SuppressWarnings("unchecked")
    final List<String> actualList =
        (List<String>) protoElp.getValue("builder.keyword.add('***1').getList()", List.class);
    log.debug("builder.keyword = '{}'", actualList);

    assertThat(actualList, is(expectedList));
  }

  @Test
  public void testGetDMBuilderBuilderList1() {
    protoElp.defineBean("builder", builder);
    protoElp.defineBean("log", log);

    final IBuilder2 expectedBuilder = builder.clone();

    protoElp.defineBean("starFD", galaxyStar);

    // List<IBuilder2> starList = expectedBuilder.getBuilderList(starFD);
    // starList.add(expectedBuilder.newBuilderForField(starFD).setAttribute("name", "New Star2"));

    @SuppressWarnings("unchecked")
    final List<IBuilder2> starList =
        (List<IBuilder2>) expectedBuilder.addFieldBuilder(galaxyStar)
            .setField(starName, "New Star2").toParent().getBuilderList(galaxyStar);
    final List<String> expectedList = Lists.newArrayList();

    for (final IBuilder2 star : starList) {
      expectedList.add((String) star.getField(starName));
    }

    @SuppressWarnings("unchecked")
    final List<String> actualList =
        (List<String>) protoElp.getValue(
            "fbuilder=builder.star; newStar=builder.newBuilderForField(starFD); newStar.name='New Star2'; "
                + "fbuilder.add(newStar).getBuilders().stream().map(e->e.name).toList()",
            List.class);
    log.debug("builder.star.name = '{}'", actualList);

    assertThat(actualList, is(expectedList));
  }

  @Test
  public void testGetDMBuilderBuilderList1_newStyle() {
    protoElp.defineBean("builder", builder);
    protoElp.defineBean("log", log);

    final IBuilder2 expectedBuilder = builder.clone();

    protoElp.defineBean("starFD", galaxyStar);

    // List<IBuilder2> starList = expectedBuilder.getBuilderList(starFD);
    // starList.add(expectedBuilder.newBuilderForField(starFD).setAttribute("name", "New Star2"));

    @SuppressWarnings("unchecked")
    final List<IBuilder2> starList =
        (List<IBuilder2>) expectedBuilder.addFieldBuilder(galaxyStar)
            .setField(starName, "New Star2").toParent().getBuilderList(galaxyStar);
    final List<String> expectedList = Lists.newArrayList();

    for (final IBuilder2 star : starList) {
      expectedList.add((String) star.getField(starName));
    }

    @SuppressWarnings("unchecked")
    final List<String> actualList =
        (List<String>) protoElp.getValue(
            "starBuilder=builder.star; starBuilder.add().name='New Star2';"
                + "starBuilder.getList().stream().map(e->e.name).toList()", List.class);
    log.debug("builder.star.name = '{}'", actualList);

    assertThat(actualList, is(expectedList));
  }

  private <T> List<T> ensureList(final List<T> list) {
    return list == null ? Collections.<T>emptyList() : list;
  }

  private <T> List<T> ensureUnmodifiableList(final List<T> list) {
    return list == null ? Collections.<T>emptyList() : Collections.unmodifiableList(list);
  }

  @Test
  public void testSetGeneratedProto1() {
    final Galaxy.Builder builder = originalMsg.toBuilder();
    elp.defineBean("builder", builder);

    final Galaxy.Builder expectedBuilder = builder.clone();
    expectedBuilder.getStarBuilder(0).setName("Star1*");
    final Galaxy expectedGalaxy = expectedBuilder.build();
    final Galaxy actualGalaxy =
        (Galaxy) elp.eval("builder.getStarBuilder(0).setName('Star1*'); builder.build()");
    log.debug("actualGalaxy = '{}'", actualGalaxy);
    assertThat(actualGalaxy, is(expectedGalaxy));
  }
}
