//
// Copyright © 2014, David Tesler (https://github.com/protobufel)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
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
import static com.github.protobufel.DynamicMessage.newBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.fictional.test.GalaxyProto.City;
import com.fictional.test.GalaxyProto.Country;
import com.fictional.test.GalaxyProto.Galaxy;
import com.fictional.test.GalaxyProto.Galaxy.Color;
import com.fictional.test.GalaxyProto.Galaxy.Star;
import com.fictional.test.GalaxyProto.Planet;
import com.fictional.test.GalaxyProto.Tag;
import com.github.protobufel.DynamicMessage.Builder;
import com.github.protobufel.test.util.ProtoUtils;
import com.google.protobuf.Message;

@State(Scope.Thread)
public class BuilderBenchmark {
  private final static int ITERATIONS_COUNT = 100;
  private Message expectedMessage;
  int dummy = 2; 
  private Message mergeStar1Message;
  private Message mergePlanet1Message;

  @Setup(Level.Trial)
  public void init() {
    expectedMessage = ProtoUtils.newGalaxy();
    mergeStar1Message = ProtoUtils.newGalaxy().getStar(0)
        .toBuilder()
        .setName("Star1*")
        .build();
    mergePlanet1Message = ProtoUtils.newGalaxy().getStar(0).getPlanet(0)
        .toBuilder()
        .setName("Planet1*")
        .build();
  }

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Message timeCreateAndBuildOriginalDM() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    com.google.protobuf.DynamicMessage.Builder builder = com.google.protobuf.DynamicMessage
        .newBuilder(expectedMessage.getDescriptorForType());
    com.google.protobuf.DynamicMessage.Builder fBuilder1;
    com.google.protobuf.DynamicMessage.Builder fBuilder2;
    com.google.protobuf.DynamicMessage.Builder fBuilder3;
    
    Message actualMessage = builder
        .setField(galaxyName, "Galaxy1")
        .setField(galaxyCode, new ArrayList<Object>(Arrays.asList(1, dummy, 100)))
        .setField(galaxyKeyword, 
            new ArrayList<Object>(Arrays.asList(
                "keyword 1", "keyword 2", "keyword 3", "keyword 4")))
        .setField(galaxyColor, Color.BLUE.getValueDescriptor())
        .addRepeatedField(galaxyStar, (fBuilder1 = builder.newBuilderForField(galaxyStar))
            .setField(starName, "Star1")
            .setField(starColor, Color.GREEN.getValueDescriptor())
            .setField(starTag, (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                .setField(tagTag, "tag1").build())
            .addRepeatedField(starPlanet, (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.YELLOW.getValueDescriptor())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build()).build())
            .addRepeatedField(starPlanet, (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet2")
                .setField(planetColor, Color.RED.getValueDescriptor())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country2")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build())
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City2").build()).build()).build()).build())

        .addRepeatedField(galaxyStar, 
            (fBuilder1 = builder.newBuilderForField(galaxyStar))
            .setField(starName, "Star2")
            .setField(starColor, Color.GREEN.getValueDescriptor())
            .setField(starTag, 
                (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                .setField(tagTag, "tag2").build())
            .addRepeatedField(starPlanet, 
                (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.GREEN.getValueDescriptor()).build())
            .addRepeatedField(starPlanet, 
                (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet2")
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country2")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build())
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City2").build()).build()).build()).build())
    .build();
    return actualMessage;
  }

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Message timeCreateAndBuildAsOriginalDM() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    Builder builder = newBuilder(expectedMessage.getDescriptorForType());
    Builder fBuilder1;
    Builder fBuilder2;
    Builder fBuilder3;
    
    Message actualMessage = builder
        .setField(galaxyName, "Galaxy1")
        .setField(galaxyCode, new ArrayList<Object>(Arrays.asList(1, dummy, 100)))
        .setField(galaxyKeyword, 
            new ArrayList<Object>(Arrays.asList(
                "keyword 1", "keyword 2", "keyword 3", "keyword 4")))
        .setField(galaxyColor, Color.BLUE.getValueDescriptor())
        .addRepeatedField(galaxyStar, (fBuilder1 = builder.newBuilderForField(galaxyStar))
            .setField(starName, "Star1")
            .setField(starColor, Color.GREEN.getValueDescriptor())
            .setField(starTag, (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                .setField(tagTag, "tag1").build())
            .addRepeatedField(starPlanet, (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.YELLOW.getValueDescriptor())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build()).build())
            .addRepeatedField(starPlanet, (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet2")
                .setField(planetColor, Color.RED.getValueDescriptor())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country2")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build())
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City2").build()).build()).build()).build())

        .addRepeatedField(galaxyStar, 
            (fBuilder1 = builder.newBuilderForField(galaxyStar))
            .setField(starName, "Star2")
            .setField(starColor, Color.GREEN.getValueDescriptor())
            .setField(starTag, 
                (fBuilder2 = fBuilder1.newBuilderForField(starTag))
                .setField(tagTag, "tag2").build())
            .addRepeatedField(starPlanet, 
                (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.GREEN.getValueDescriptor()).build())
            .addRepeatedField(starPlanet, 
                (fBuilder2 = fBuilder1.newBuilderForField(starPlanet))
                .setField(planetName, "Planet2")
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country1")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build()).build())
                .addRepeatedField(planetCountry, 
                    (fBuilder3 = fBuilder2.newBuilderForField(planetCountry))
                    .setField(countryName, "Country2")
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City1").build())
                    .addRepeatedField(countryCity, fBuilder3.newBuilderForField(countryCity)
                        .setField(cityName, "City2").build()).build()).build()).build())
    .build();
    return actualMessage;
  }
  
  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Message timeCreateAndBuildAsOriginalGM() {
    dummy = ++dummy % Integer.MAX_VALUE;
 
    return newBuilder(expectedMessage.getDescriptorForType())
        .setField(galaxyName, "Galaxy1")
        .addAllRepeatedField(galaxyCode, Arrays.asList(1, dummy, 100))
        .addAllRepeatedField(galaxyKeyword, Arrays.asList("keyword 1", "keyword 2", "keyword 3", 
            "keyword 4"))
        .setField(galaxyColor, Color.BLUE)
        .addFieldBuilder(galaxyStar)
            .setField(starName, "Star1")
            .setField(starColor, Color.GREEN)
            .getFieldBuilder(starTag)
                .setField(tagTag, "tag1")
                .toParent()
            .addFieldBuilder(starPlanet)
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.YELLOW)
                .addFieldBuilder(planetCountry)
                    .setField(countryName, "Country1")
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City1")
                        .toParent()
                    .toParent()
                .toParent()
            .addFieldBuilder(starPlanet)
                .setField(planetName, "Planet2")
                .setField(planetColor, Color.RED)
                .addFieldBuilder(planetCountry)
                    .setField(countryName, "Country1")
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City1")
                        .toParent()
                    .toParent()
                .addFieldBuilder(planetCountry)
                    .setField(countryName, "Country2")
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City1")
                        .toParent()
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City2")
                        .toParent()
                    .toParent()
                .toParent()
             .toParent()   
         .addFieldBuilder(galaxyStar)
            .setField(starName, "Star2")
            .setField(starColor, Color.GREEN)
            .getFieldBuilder(starTag)
                .setField(tagTag, "tag2")
                .toParent()
            .addFieldBuilder(starPlanet)
                .setField(planetName, "Planet1")
                .setField(planetColor, Color.GREEN)
                .toParent()
            .addFieldBuilder(starPlanet)
                .setField(planetName, "Planet2")
                .addFieldBuilder(planetCountry)
                    .setField(countryName, "Country1")
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City1")
                        .toParent()
                    .toParent()
                .addFieldBuilder(planetCountry)
                    .setField(countryName, "Country2")
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City1")
                        .toParent()
                    .addFieldBuilder(countryCity)
                        .setField(cityName, "City2")
                        .toParent()
                    .toParent()
                .toParent()
            .toParent()   
        .build();
  }
  
  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Galaxy timeCreateAndBuildOriginalGM() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    return Galaxy.newBuilder()
        .setName("Galaxy1")
        .addCode(1)
        .addCode(dummy)
        .addCode(100)
        .addKeyword("keyword 1")
        .addKeyword("keyword 2")
        .addKeyword("keyword 3")
        .addKeyword("keyword 4")
        .setColor(Color.BLUE)
        .addStar(Star.newBuilder()
            .setName("Star1")
            .setColor(Color.GREEN)
            .setTag(Tag.newBuilder().setTag("tag1"))
            .addPlanet(Planet.newBuilder()
                .setName("Planet1")
                .setColor(Color.YELLOW)
                .addCountry(Country.newBuilder()
                    .setName("Country1")
                    .addCity(City.newBuilder()
                        .setName("City1")
                        )
                    )
                )
            .addPlanet(Planet.newBuilder()
                .setName("Planet2")
                .setColor(Color.RED)
                .addCountry(Country.newBuilder()
                    .setName("Country1")
                    .addCity(City.newBuilder()
                        .setName("City1")
                        )
                    )
                .addCountry(Country.newBuilder()
                    .setName("Country2")
                    .addCity(City.newBuilder()
                        .setName("City1")
                        )
                    .addCity(City.newBuilder()
                        .setName("City2")
                        )
                    )
                )
            )
        .addStar(Star.newBuilder()
            .setName("Star2")
            .setColor(Color.GREEN)
            .setTag(Tag.newBuilder().setTag("tag2"))
            .addPlanet(Planet.newBuilder()
                .setName("Planet1")
                .setColor(Color.GREEN)
                )
            .addPlanet(Planet.newBuilder()
                .setName("Planet2")
                .addCountry(Country.newBuilder()
                    .setName("Country1")
                    .addCity(City.newBuilder()
                        .setName("City1")
                        )
                    )
                .addCountry(Country.newBuilder()
                    .setName("Country2")
                    .addCity(City.newBuilder()
                        .setName("City1")
                        )
                    .addCity(City.newBuilder()
                        .setName("City2")
                        )
                    )
                )
            )
        .build();
  }
  
  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Message timeMergeAndBuild() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    Builder builder = newBuilder(expectedMessage)
        .getFieldBuilder(galaxyStar, 0)
            .setField(starName, String.valueOf(dummy))
            .mergeFrom(mergeStar1Message)
            .getFieldBuilder(starPlanet, 0)
                .mergeFrom(mergePlanet1Message)
                .toParent()
            .toParent();
    
    return builder.build();
  }  
  
  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Message timeMergeAndBuildOriginalDM() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    com.google.protobuf.DynamicMessage.Builder builder = com.google.protobuf.DynamicMessage
        .newBuilder(expectedMessage);
    
    com.google.protobuf.DynamicMessage.Builder starBuilder = com.google.protobuf.DynamicMessage
        .newBuilder((Message) builder.getRepeatedField(galaxyStar, 0));
    
    Message planetMessage = (Message) starBuilder
        .setField(MessageAdapter.getFieldDescriptor(starBuilder, "name"), String.valueOf(dummy))
        .mergeFrom(mergeStar1Message)
        .getRepeatedField(starPlanet, 0);
    planetMessage = com.google.protobuf.DynamicMessage
        .newBuilder(planetMessage)
        .mergeFrom(mergePlanet1Message).build();
    
    Message actualMessage = builder.setRepeatedField(
        galaxyStar, 
        0, 
        starBuilder.setRepeatedField(starPlanet, 0, planetMessage).build())
        .build();
    return actualMessage;
  }  

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MICROSECONDS)
  @Measurement(iterations = ITERATIONS_COUNT, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
  @Fork(1)
  public Galaxy timeMergeAndBuildOriginalGM() {
    dummy = ++dummy % Integer.MAX_VALUE;
    
    Galaxy.Builder builder = Galaxy.newBuilder((Galaxy) expectedMessage);
    builder.getStarBuilder(0)
        .setName(String.valueOf(dummy))
        .mergeFrom(mergeStar1Message)
        .getPlanetBuilder(0)
        .mergeFrom(mergePlanet1Message);
    
    return builder.build();    
  }
}
