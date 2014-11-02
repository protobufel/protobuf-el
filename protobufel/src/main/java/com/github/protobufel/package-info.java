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

/**
 * <p>
 * Provides DynamicMessage and DynamicMessage.Builder with additional capabilities compared to the
 * corresponding original Google Protocol Buffer implementation, and can be used as the drop-in
 * replacement of the originals.
 * <p>
 * In addition to the standard features required of any Message.Builder, DynamicMessage.Builder has
 * the following:
 * <ol>
 * <li>hierarchical builders via getFieldBuilder method
 * <li>almost all extra of the GeneratedMessage.Builder(-s)
 * <li>convenience methods accepting String in place of FieldDescriptor
 * <li>methods for getting and setting attributes and children instead of just any field type
 * <li>flow/navigation support, like toChild and toParent methods
 * <li>automatic invalidation of the root builder and its child builders
 * </ol>
 *
 * Some of these capabilities are essential, and will be used for the adaptation of the Protocol
 * Buffers in JSR-341 EL 3.0 language.
 *
 * @see https://jcp.org/aboutJava/communityprocess/final/jsr341/index.html
 * @see https://code.google.com/p/protobuf/
 *
 */
package com.github.protobufel;

