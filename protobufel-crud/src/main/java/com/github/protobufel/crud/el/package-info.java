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
 * Provides basic query processing capabilities using EL 3.0 expression language with a mix of
 * regular objects and ProtoBuf Messages and Builders. 
 * <p><h2>Usage</h2>:
 * Get a list of ProtoBuf Message(s) as an in-parameter, with an optional map of beans, and as the
 * result return a list of ProtoBuf Message(s), processed via EL 3.0 language. This, effectively,
 * can be any CRUD operation, or a combination of thereof.
 * 
 * <pre>
 * List<Message> actual = ProtoMessageQueryProcessor.builder()
 *     .setExpression("(index==0) ? (record.color='RED') : null; record")
 *     .process(originalCityList);
 * </pre>
 *
 * @see <a href="doc-files/Examples.java.txt">Examples.java</a>
 * @see <a href="doc-files/galaxy.proto.txt">galaxy.proto sample</a>
 * @see <a href="https://jcp.org/aboutJava/communityprocess/final/jsr341/index.html">JSR-000341 Expression Language 3.0</a>
 * @see <a href="https://developers.google.com/protocol-buffers/docs/overview">Protocol Buffers</a>
 *
 * @author protobufel@gmail.com David Tesler
 */
package com.github.protobufel.crud.el;
