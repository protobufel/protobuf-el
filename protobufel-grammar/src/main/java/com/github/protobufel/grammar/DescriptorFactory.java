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

package com.github.protobufel.grammar;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

// TODO remove if not needed
final class DescriptorFactory {
  private DescriptorFactory() {}

  public static Descriptor findDescriptor(final FileDescriptor fileDescriptor,
      final String messageTypeName) {
    if (messageTypeName == null || fileDescriptor == null) {
      throw new NullPointerException();
    }

    for (final Descriptor descriptor : fileDescriptor.getMessageTypes()) {
      if (messageTypeName.equals(descriptor.getFullName())) {
        return descriptor;
      }
    }

    for (final Descriptor descriptor : fileDescriptor.getMessageTypes()) {
      findDescriptor(descriptor, messageTypeName);
    }

    return null;
  }

  public static Descriptor findDescriptor(final Descriptor descriptor, final String messageTypeName) {
    for (final Descriptor child : descriptor.getNestedTypes()) {
      if (messageTypeName.equals(child.getFullName())) {
        return descriptor;
      }
    }

    for (final Descriptor child : descriptor.getNestedTypes()) {
      final Descriptor foundDescriptor = findDescriptor(child, messageTypeName);

      if (foundDescriptor != null) {
        return foundDescriptor;
      }
    }

    return null;
  }
}
