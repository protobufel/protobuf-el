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

import static com.github.protobufel.common.verifications.Verifications.assertNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fictional.test.GalaxyProto.Galaxy;
import com.github.protobufel.test.util.ProtoUtils;
import com.google.common.io.Resources;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

@RunWith(JUnit4.class)
public class DynamicMessageSerializationTest {
  private static final Logger log = LoggerFactory.getLogger(DynamicMessageSerializationTest.class);
  private static final Descriptor FILESET_DESCRIPTOR = FileDescriptorSet.getDescriptor();
  private static final String FILE_DESCRIPTOR_SET_PATH = "/META-INF/FileDescriptorSet";
  private Message expectedMessage;
  private IDynamicMessageProvider messageProvider;

  @Before
  public void init() {
    messageProvider = DynamicMessage.getProvider();
    expectedMessage = parseFileDescriptorSet(true);
  }

  @Test
  public void testGeneratedDeSerialization() throws IOException {
    final String value = "GalaxyName1";
    final Galaxy message = ProtoUtils.newGalaxy().newBuilderForType().setName(value).buildPartial();
    assertThat(message.getName(), equalTo(value));
  }


  @Test
  public void testFileDescriptorSetAccess() {
    final URL fileSetPath = ProtoUtils.class.getResource(FILE_DESCRIPTOR_SET_PATH);
    assertThat("FileDescriptorSet' path is empty", fileSetPath != null
        && !fileSetPath.getFile().isEmpty());
  }

  @Test
  public void testFileDescriptorSetDeSerialization() throws IOException {
    final Message actualMessage = parseFileDescriptorSet(false);
    assertThat("FileDescriptorSet' message is empty", actualMessage != null);
    assertThat("Wrong FileDescriptorSet' message type", assertNonNull(actualMessage)
        .getDescriptorForType(), equalTo(FILESET_DESCRIPTOR));
    assertThat(actualMessage, equalTo(expectedMessage));
    // log.error("actual message: {}", actualMessage.toString());
  }

  private Message parseFileDescriptorSet(final boolean useOriginal) {
    return parseMessage(ProtoUtils.class, FILE_DESCRIPTOR_SET_PATH, FILESET_DESCRIPTOR, useOriginal);
  }

  private Message parseMessage(final Class<?> contextClass, final String messageFilePath,
      final Descriptor descriptor, final boolean useOriginal) {
    Message message;

    try {
      final byte[] bytes =
          Resources.toByteArray(Resources.getResource(contextClass, messageFilePath));
      if (useOriginal) {
        message = com.google.protobuf.DynamicMessage.parseFrom(descriptor, bytes);
      } else {
        message = messageProvider.parseFrom(descriptor, bytes);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    // log.debug("{} message: {}", (useOriginal ? "expected" : "actual"), message.toString());
    return message;
  }
}
