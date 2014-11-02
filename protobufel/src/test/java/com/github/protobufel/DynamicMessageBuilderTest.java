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

package com.github.protobufel;

import static com.github.protobufel.test.util.ProtoUtils.cityName;
import static com.github.protobufel.test.util.ProtoUtils.countryCity;
import static com.github.protobufel.test.util.ProtoUtils.countryName;
import static com.github.protobufel.test.util.ProtoUtils.galaxyCode;
import static com.github.protobufel.test.util.ProtoUtils.galaxyColor;
import static com.github.protobufel.test.util.ProtoUtils.galaxyKeyword;
import static com.github.protobufel.test.util.ProtoUtils.galaxyName;
import static com.github.protobufel.test.util.ProtoUtils.galaxyStar;
import static com.github.protobufel.test.util.ProtoUtils.planetColor;
import static com.github.protobufel.test.util.ProtoUtils.planetCountry;
import static com.github.protobufel.test.util.ProtoUtils.planetName;
import static com.github.protobufel.test.util.ProtoUtils.starColor;
import static com.github.protobufel.test.util.ProtoUtils.starName;
import static com.github.protobufel.test.util.ProtoUtils.starPlanet;
import static com.github.protobufel.test.util.ProtoUtils.starTag;
import static com.github.protobufel.test.util.ProtoUtils.tagTag;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fictional.test.GalaxyProto.Galaxy;
import com.fictional.test.GalaxyProto.Galaxy.Color;
import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.test.util.ProtoUtils;
import com.google.protobuf.Message;

@RunWith(JUnit4.class)
public class DynamicMessageBuilderTest {
  private static final Logger log = LoggerFactory.getLogger(DynamicMessageBuilderTest.class);
  private Message expectedMessage;
  private Message mergeStar1Message;
  private Message mergePlanet1Message;
  private Message expectedMergedMessage;
  private IBuilder2 builder;
  private IDynamicMessageProvider messageProvider;

  @Before
  public void init() {
    messageProvider = DynamicMessage.getProvider();
    expectedMessage = ProtoUtils.newGalaxy();
    mergeStar1Message = ProtoUtils.newGalaxy().getStar(0).toBuilder().setName("Star1*").build();
    mergePlanet1Message =
        ProtoUtils.newGalaxy().getStar(0).getPlanet(0).toBuilder().setName("Planet1*").build();
    expectedMergedMessage = getExpectedMergedMessage();
    builder = messageProvider.newBuilder(expectedMessage);
  }

  private Galaxy getExpectedMergedMessage() {
    final Galaxy.Builder galaxyBuilder = ((Galaxy) expectedMessage).toBuilder();
    galaxyBuilder.getStarBuilder(0).mergeFrom(mergeStar1Message).getPlanetBuilder(0)
        .mergeFrom(mergePlanet1Message);
    return galaxyBuilder.build();
  }

  @Test
  public void testCreateAndBuild() {
    final Message actualMessage =
        messageProvider
            .newBuilder(expectedMessage.getDescriptorForType())
            .setField(galaxyName, "Galaxy1")
            .addAllRepeatedField(galaxyCode, Arrays.asList(1, 2, 100))
            .addAllRepeatedField(galaxyKeyword,
                Arrays.asList("keyword 1", "keyword 2", "keyword 3", "keyword 4"))
            .setField(galaxyColor, Color.BLUE).addFieldBuilder(galaxyStar)
            .setField(starName, "Star1").setField(starColor, Color.GREEN).getFieldBuilder(starTag)
            .setField(tagTag, "tag1").toParent().addFieldBuilder(starPlanet)
            .setField(planetName, "Planet1").setField(planetColor, Color.YELLOW)
            .addFieldBuilder(planetCountry).setField(countryName, "Country1")
            .addFieldBuilder(countryCity).setField(cityName, "City1").toParent().toParent()
            .toParent().addFieldBuilder(starPlanet).setField(planetName, "Planet2")
            .setField(planetColor, Color.RED).addFieldBuilder(planetCountry)
            .setField(countryName, "Country1").addFieldBuilder(countryCity)
            .setField(cityName, "City1").toParent().toParent().addFieldBuilder(planetCountry)
            .setField(countryName, "Country2").addFieldBuilder(countryCity)
            .setField(cityName, "City1").toParent().addFieldBuilder(countryCity)
            .setField(cityName, "City2").toParent().toParent().toParent().toParent()
            .addFieldBuilder(galaxyStar).setField(starName, "Star2")
            .setField(starColor, Color.GREEN).getFieldBuilder(starTag).setField(tagTag, "tag2")
            .toParent().addFieldBuilder(starPlanet).setField(planetName, "Planet1")
            .setField(planetColor, Color.GREEN).toParent().addFieldBuilder(starPlanet)
            .setField(planetName, "Planet2").addFieldBuilder(planetCountry)
            .setField(countryName, "Country1").addFieldBuilder(countryCity)
            .setField(cityName, "City1").toParent().toParent().addFieldBuilder(planetCountry)
            .setField(countryName, "Country2").addFieldBuilder(countryCity)
            .setField(cityName, "City1").toParent().addFieldBuilder(countryCity)
            .setField(cityName, "City2").toParent().toParent().toParent().toParent().build();

    // String actualMessageString = actualMessage.toString();
    assertThat(actualMessage, equalTo(expectedMessage));
  }

  @Test
  public void testMergeAndBuild() {
    final Message actualMessage =
        builder.getFieldBuilder(galaxyStar, 0).setField(starName, "Star1")
        .mergeFrom(mergeStar1Message).getFieldBuilder(starPlanet, 0)
            .mergeFrom(mergePlanet1Message).toParent().toParent().build();

    // String actualMessageString = actualMessage.toString();
    assertThat(actualMessage, equalTo(expectedMergedMessage));
  }

  @Test
  public void testOriginalDMCreateAndBuild() {
    final com.google.protobuf.DynamicMessage.Builder builder =
        com.google.protobuf.DynamicMessage.newBuilder(expectedMessage.getDescriptorForType());
    com.google.protobuf.DynamicMessage.Builder fBuilder1;
    com.google.protobuf.DynamicMessage.Builder fBuilder2;
    com.google.protobuf.DynamicMessage.Builder fBuilder3;

    final Message actualMessage =
        builder
            .setField(galaxyName, "Galaxy1")
            .setField(galaxyCode, new ArrayList<Object>(Arrays.asList(1, 2, 100)))
            .setField(
                galaxyKeyword,
                new ArrayList<Object>(Arrays.asList("keyword 1", "keyword 2", "keyword 3",
                    "keyword 4")))
            .setField(galaxyColor, Color.BLUE.getValueDescriptor())
            .addRepeatedField(
                galaxyStar,
                (fBuilder1 = builder.newBuilderForField(galaxyStar))
                    .setField(starName, "Star1")
                    .setField(starColor, Color.GREEN.getValueDescriptor())
                    .setField(
                        starTag,
                        (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                            .setField(tagTag, "tag1").build())
                    .addRepeatedField(
                        starPlanet,
                        (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                            .setField(planetName, "Planet1")
                            .setField(planetColor, Color.YELLOW.getValueDescriptor())
                            .addRepeatedField(
                                planetCountry,
                                (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                                    .setField(countryName, "Country1")
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City1").build()).build()).build())
                    .addRepeatedField(
                        starPlanet,
                        (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                            .setField(planetName, "Planet2")
                            .setField(planetColor, Color.RED.getValueDescriptor())
                            .addRepeatedField(
                                planetCountry,
                                (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                                    .setField(countryName, "Country1")
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City1").build()).build())
                            .addRepeatedField(
                                planetCountry,
                                (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                                    .setField(countryName, "Country2")
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City1").build())
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City2").build()).build()).build())
                    .build())

            .addRepeatedField(
                galaxyStar,
                (fBuilder1 = builder.newBuilderForField(galaxyStar))
                    .setField(starName, "Star2")
                    .setField(starColor, Color.GREEN.getValueDescriptor())
                    .setField(
                        starTag,
                        (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                            .setField(tagTag, "tag2").build())
                    .addRepeatedField(
                        starPlanet,
                        (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                            .setField(planetName, "Planet1")
                            .setField(planetColor, Color.GREEN.getValueDescriptor()).build())
                    .addRepeatedField(
                        starPlanet,
                        (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                            .setField(planetName, "Planet2")
                            .addRepeatedField(
                                planetCountry,
                                (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                                    .setField(countryName, "Country1")
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City1").build()).build())
                            .addRepeatedField(
                                planetCountry,
                                (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                                    .setField(countryName, "Country2")
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City1").build())
                                    .addRepeatedField(
                                        countryCity,
                                        fBuilder3.newBuilderForField(countryCity)
                                            .setField(cityName, "City2").build()).build()).build())
                    .build()).build();
    // String actualMessageString = actualMessage.toString();
    log.info(actualMessage.toString());
    assertThat(actualMessage, equalTo(expectedMessage));
  }
}
