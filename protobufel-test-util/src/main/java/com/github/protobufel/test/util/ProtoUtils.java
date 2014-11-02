// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.github.protobufel.test.util;


import com.fictional.test.Fields1Proto.EnumField1;
import com.fictional.test.Fields1Proto.ScalarAndEnumFields;
import com.fictional.test.Fields1Proto.ScalarEnumMessageFields;
import com.fictional.test.Fields1Proto.ScalarFields;
import com.fictional.test.GalaxyProto.City;
import com.fictional.test.GalaxyProto.Country;
import com.fictional.test.GalaxyProto.Galaxy;
import com.fictional.test.GalaxyProto.Galaxy.Color;
import com.fictional.test.GalaxyProto.Galaxy.Star;
import com.fictional.test.GalaxyProto.Planet;
import com.fictional.test.GalaxyProto.Tag;
import com.fictional.test.MessageWithBytes1Proto.MessageWithBytes1;
import com.fictional.test.SubMessageOneProto.SubMessageOne;
import com.fictional.test.SubMessageOneProto.SubMessageOne.SimpleMessage;
import com.fictional.test.SubMessageStandaloneOneProto;
import com.fictional.test.SubMessageStandaloneOneProto.SubMessageStandaloneOne;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.WireFormat;

public class ProtoUtils {
  public static final FieldDescriptor galaxyName;
  public static final FieldDescriptor galaxyColor;
  public static final FieldDescriptor galaxyCode;
  public static final FieldDescriptor galaxyKeyword;
  public static final FieldDescriptor galaxyStar;

  public static final FieldDescriptor starName;
  public static final FieldDescriptor starColor;
  public static final FieldDescriptor starTag;
  public static final FieldDescriptor starPlanet;

  public static final FieldDescriptor tagTag;
  
  public static final FieldDescriptor planetName;
  public static final FieldDescriptor planetColor;
  public static final FieldDescriptor planetCountry;
  
  public static final FieldDescriptor countryName;
  public static final FieldDescriptor countryCity;
  
  public static final FieldDescriptor cityName;
  public static final FieldDescriptor starKeyword;
  
  static {
    Descriptor type = Galaxy.getDescriptor();
    galaxyName = type.findFieldByName("name");
    galaxyColor = type.findFieldByName("color");
    galaxyCode = type.findFieldByName("code");
    galaxyKeyword = type.findFieldByName("keyword");
    galaxyStar = type.findFieldByName("star");

    type = Star.getDescriptor();
    starName = type.findFieldByName("name");
    starColor = type.findFieldByName("color");
    starTag = type.findFieldByName("tag");
    starPlanet = type.findFieldByName("planet");
    starKeyword = type.findFieldByName("keyword");

    type = Tag.getDescriptor();
    tagTag = type.findFieldByName("tag");
    
    type = Planet.getDescriptor();
    planetName = type.findFieldByName("name");
    planetColor = type.findFieldByName("color");
    planetCountry = type.findFieldByName("country");
    
    type = Country.getDescriptor();
    countryName = type.findFieldByName("name");
    countryCity = type.findFieldByName("city");
    
    type = City.getDescriptor();
    cityName = type.findFieldByName("name");
  }
 
	public static Galaxy newGalaxy() {
		return Galaxy.newBuilder()
				.setName("Galaxy1")
				.addCode(1)
				.addCode(2)
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
	
	public static SubMessageOne newMessageOne() {
		return SubMessageOne.newBuilder()
				.setMsg(SimpleMessage.newBuilder()
						.setName("simple1")
						.setId(1))
				.build();
	}

	public static SimpleMessage newSimpleMessageOne() {
		return SimpleMessage.newBuilder()
						.setName("simple1")
						.setId(1)
						.build();
	}
	
	public static SubMessageStandaloneOne newMessageStanaloneOne() {
		return SubMessageStandaloneOne.newBuilder()
				.setMsg(SubMessageStandaloneOneProto.SimpleMessage.newBuilder()
						.setName("simple1")
						.setId(1))
				.build();
	}

	public static SubMessageStandaloneOneProto.SimpleMessage newSimpleMessageStandaloneOne() {
		return SubMessageStandaloneOneProto.SimpleMessage.newBuilder()
						.setName("simple1")
						.setId(1)
						.build();
	}

	public static MessageWithBytes1 newMessageWithBytes1(ByteString bytes) {
		return MessageWithBytes1.newBuilder().setBytes(bytes).build();
	}
	
	public static ScalarFields newScalarFields() {
	  return ScalarFields.newBuilder()
	      .setBooleanField(true)
	      .setDoubleField(-1.1D)
	      .setFixed32Field(111)
	      .setFixed64Field(111111)
	      .setFloatField(-1.2F)
	      .setInt32Field(-222)
	      .setInt64Field(-222222)
	      .setSfixed32Field(333)
	      .setSfixed64Field(333333)
	      .setSint32Field(444)
	      .setSint64Field(444444)
	      .setUint32Field(555)
	      .setUint64Field(555555)
        .setBytesField(ByteString.copyFromUtf8("Hello!"))
        .setStringField("Hello!")
        .build();
	}

  public static ScalarAndEnumFields newScalarEndEnumFields() {
    return ScalarAndEnumFields.newBuilder()
        .setBooleanField(true)
        .setDoubleField(-1.1D)
        .setFixed32Field(111)
        .setFixed64Field(111111)
        .setFloatField(-1.2F)
        .setInt32Field(-222)
        .setInt64Field(-222222)
        .setSfixed32Field(333)
        .setSfixed64Field(333333)
        .setSint32Field(444)
        .setSint64Field(444444)
        .setUint32Field(555)
        .setUint64Field(555555)
        .setBytesField(ByteString.copyFromUtf8("Hello!"))
        .setStringField("Hello!")
        .setEnumField(EnumField1.ONE)
        .build();
  }

  public static ScalarFields newSingleScalarFields(WireFormat.FieldType type) {
    ScalarFields.Builder builder = ScalarFields.newBuilder();
    
    switch (type) {
      case BOOL:
        builder.setBooleanField(true);
        break;
      case BYTES:
        builder.setBytesField(ByteString.copyFromUtf8("Hello!"));
        break;
      case DOUBLE:
        builder.setDoubleField(-1.1D);
        break;
      case FIXED32:
        builder.setFixed32Field(111);
        break;
      case FIXED64:
        builder.setFixed64Field(111111);
        break;
      case FLOAT:
        builder.setFloatField(-1.2F);
        break;
      case INT32:
        builder.setInt32Field(-222);
        break;
      case INT64:
        builder.setInt64Field(-222222);
        break;
      case SFIXED32:
        builder.setSfixed32Field(333);
        break;
      case SFIXED64:
        builder.setSfixed64Field(333333);
        break;
      case SINT32:
        builder.setSint32Field(444);
        break;
      case SINT64:
        builder.setSint64Field(444444);
        break;
      case STRING:
        builder.setStringField("Hello!");
        break;
      case UINT32:
        builder.setUint32Field(555);
        break;
      case UINT64:
        builder.setUint64Field(555555);
        break;
      case ENUM:
      case MESSAGE:
      case GROUP:
      default:
        break;
    }
    
    return builder.build();
  }

  public static ScalarAndEnumFields newSingleScalarAndEnumFields(WireFormat.FieldType type) {
    ScalarAndEnumFields.Builder builder = ScalarAndEnumFields.newBuilder();
    
    switch (type) {
      case BOOL:
        builder.setBooleanField(true);
        break;
      case BYTES:
        builder.setBytesField(ByteString.copyFromUtf8("Hello!"));
        break;
      case DOUBLE:
        builder.setDoubleField(-1.1D);
        break;
      case FIXED32:
        builder.setFixed32Field(111);
        break;
      case FIXED64:
        builder.setFixed64Field(111111);
        break;
      case FLOAT:
        builder.setFloatField(-1.2F);
        break;
      case INT32:
        builder.setInt32Field(-222);
        break;
      case INT64:
        builder.setInt64Field(-222222);
        break;
      case SFIXED32:
        builder.setSfixed32Field(333);
        break;
      case SFIXED64:
        builder.setSfixed64Field(333333);
        break;
      case SINT32:
        builder.setSint32Field(444);
        break;
      case SINT64:
        builder.setSint64Field(444444);
        break;
      case STRING:
        builder.setStringField("Hello!");
        break;
      case UINT32:
        builder.setUint32Field(555);
        break;
      case UINT64:
        builder.setUint64Field(555555);
        break;
      case ENUM:
        builder.setEnumField(EnumField1.ONE);
        break;
      case GROUP:
      case MESSAGE:
      default:
        break;
    }
    
    return builder.build();
  }

  public static ScalarEnumMessageFields newScalarEnumMessageFields(boolean setMessageField) {
    ScalarEnumMessageFields.Builder builder = ScalarEnumMessageFields.newBuilder()
        .setBooleanField(true)
        .setDoubleField(-1.1D)
        .setFixed32Field(111)        .setFixed64Field(111111)
        .setFloatField(-1.2F)
        .setInt32Field(-222)
        .setInt64Field(-222222)
        .setSfixed32Field(333)
        .setSfixed64Field(333333)
        .setSint32Field(444)
        .setSint64Field(444444)
        .setUint32Field(555)
        .setUint64Field(555555)
        .setBytesField(ByteString.copyFromUtf8("Hello!"))
        .setStringField("Hello!")
        .setEnumField(EnumField1.ONE);
        
    if (setMessageField) {
      builder.setMessageField(newScalarEnumMessageFields(false));
    }
      
    return builder.build();
  }
  
  public static ScalarEnumMessageFields newSingleScalarEnumMessageFields(WireFormat.FieldType type) {
    ScalarEnumMessageFields.Builder builder = ScalarEnumMessageFields.newBuilder();
    
    switch (type) {
      case BOOL:
        builder.setBooleanField(true);
        break;
      case BYTES:
        builder.setBytesField(ByteString.copyFromUtf8("Hello!"));
        break;
      case DOUBLE:
        builder.setDoubleField(-1.1D);
        break;
      case FIXED32:
        builder.setFixed32Field(111);
        break;
      case FIXED64:
        builder.setFixed64Field(111111);
        break;
      case FLOAT:
        builder.setFloatField(-1.2F);
        break;
      case INT32:
        builder.setInt32Field(-222);
        break;
      case INT64:
        builder.setInt64Field(-222222);
        break;
      case SFIXED32:
        builder.setSfixed32Field(333);
        break;
      case SFIXED64:
        builder.setSfixed64Field(333333);
        break;
      case SINT32:
        builder.setSint32Field(444);
        break;
      case SINT64:
        builder.setSint64Field(444444);
        break;
      case STRING:
        builder.setStringField("Hello!");
        break;
      case UINT32:
        builder.setUint32Field(555);
        break;
      case UINT64:
        builder.setUint64Field(555555);
        break;
      case ENUM:
        builder.setEnumField(EnumField1.ONE);
        break;
      case MESSAGE:
        builder.setMessageField(newScalarEnumMessageFields(false));
        break;
      case GROUP:
      default:
        break;
    }
    
    return builder.build();
  }
}
