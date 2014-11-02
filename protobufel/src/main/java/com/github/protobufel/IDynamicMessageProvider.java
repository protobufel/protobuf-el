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

import java.io.IOException;
import java.io.InputStream;

import com.github.protobufel.ProtoInterfaces.IBuilder2;
import com.github.protobufel.ProtoInterfaces.IMessageEx;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public interface IDynamicMessageProvider {

  /** Construct a {@link Message.Builder} for the given type. */
  public IBuilder2 newBuilder(Descriptor type);

  /**
   * Construct a {@link Message.Builder} for a message of the same type as {@code prototype}, and
   * initialize it with {@code prototype}'s contents.
   */
  public IBuilder2 newBuilder(Message message);

  /**
   * Get a {@code DynamicMessage} representing the default instance of the given type.
   */
  public IMessageEx getDefaultInstance(Descriptor type);

  /** Parse {@code data} as a message of the given type and return it. */
  public IMessageEx parseFrom(Descriptor descriptor, ByteString rawBytes)
      throws InvalidProtocolBufferException;

  /** Parse {@code data} as a message of the given type and return it. */
  public IMessageEx parseFrom(Descriptor descriptor, ByteString rawBytes,
      ExtensionRegistry extensionRegistry) throws InvalidProtocolBufferException;

  /** Parse {@code data} as a message of the given type and return it. */
  public IMessageEx parseFrom(Descriptor descriptor, byte[] bytes)
      throws InvalidProtocolBufferException;

  /** Parse a message of the given type from the given input stream. */
  public IMessageEx parseFrom(Descriptor type, CodedInputStream input) throws IOException;

  /** Parse a message of the given type from the given input stream. */
  public IMessageEx parseFrom(Descriptor type, CodedInputStream input,
      ExtensionRegistry extensionRegistry) throws IOException;

  /** Parse {@code data} as a message of the given type and return it. */
  public IMessageEx parseFrom(Descriptor type, byte[] data, ExtensionRegistry extensionRegistry)
      throws InvalidProtocolBufferException;

  /** Parse a message of the given type from {@code input} and return it. */
  public IMessageEx parseFrom(Descriptor type, InputStream input) throws IOException;

  /** Parse a message of the given type from {@code input} and return it. */
  public IMessageEx parseFrom(Descriptor type, InputStream input,
      ExtensionRegistry extensionRegistry) throws IOException;
}
