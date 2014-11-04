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
 * Enables the ProtoBuf Messages and Builders in JSR-341 EL 3.0 language.
 * <p>
 * Use ProtoELProcessorEx in place of the original ELProcessor for processing both regular objects
 * and ProtoBuf Messages and Builders.
 * <p>
 * Support for EL-friendly ProtoBuf Attribute/Builder/Message Lists. Provides a special object when
 * a repeated field is requested on a protoBuf Builder. This object has the following methods and
 * EL/JavaBean properties:
 * <ol>
 * <li>getMessages()/messages - immutable list/view of Messages
 * <li>getBuilders()/builders - immutable list/view of Builders
 * <li>add() - adds a new default/empty Builder and returns it
 * <li>addMessage(index, Message) - adds the Message and returns its Builder
 * <li>addBuilder(index, Builder)
 * <li>setMessage(index, Message)
 * <li>setBuilder(index, Builder)
 * <li>remove(index)
 * <li>clear()
 * <li>getParent()/parent
 * <li>getChanged()/changed
 * <li>getChangedIndex()/changedIndex
 * </ol>
 * <p>
 * Sub-alternative:
 * <ol>
 * <li>getMessages()/messages - immutable list/view of Messages
 * <li>getBuilders()/builders - immutable list/view of Builders
 * <li>add() - adds a new default/empty Builder and returns it
 * <li>add(index, Message) - adds the Message and returns back to the object
 * <li>add(index, Builder)
 * <li>set(index, Message)
 * <li>set(index, Builder)
 * <li>remove(index)
 * <li>clear()
 * <li>getParent()/parent
 * <li>getChanged()/changed
 * <li>getChangedIndex()/changedIndex
 * </ol>
 * Requires handling overloaded methods, but is more concise and consistent with attribute lists
 * which have the following:
 * <ol>
 * <li>add() - adds the default value for the type
 * <li>add(index, value) - adds the value and returns back to the object
 * <li>set(index, value)
 * <li>remove(index)
 * <li>clear()
 * <li>getParent()/parent
 * <li>getChanged()/changed
 * <li>getChangedIndex()/changedIndex
 * </ol>
 * <p>
 * As an alternative, the above features could have been implemented on a top of the returned
 * attribute, Builder, or Message list. However, the current EL collection operations don't support
 * mutation, so this would not be very beneficial, apart from being more concise. Also, this would
 * confuse what type of collection being returned - for a Message base object it must be a list of
 * Messages, but for Builder base - could be either a list of Builders or Messages, and how to get
 * the other type? And the last, it would complicate implementation a little bit.
 * <p>
 * The provided main approach leaves door open for incorporation of the mutation methods directly
 * into the lists.
 *
 * @see <a href="doc-files/Examples.java.txt">Examples.java</a>
 * @see <a href="doc-files/galaxy.proto.txt">galaxy.proto sample</a>
 * @see <a href="https://jcp.org/aboutJava/communityprocess/final/jsr341/index.html">JSR-000341
 *      Expression Language 3.0</a>
 * @see <a href="https://developers.google.com/protocol-buffers/docs/overview">Protocol Buffers</a>
 *
 * @author protobufel@gmail.com David Tesler
 */
package com.github.protobufel.el;

