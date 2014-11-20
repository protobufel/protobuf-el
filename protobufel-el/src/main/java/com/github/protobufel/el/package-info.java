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
 * <p>The following rules apply to any ProtoBuf {@link Message} and {@link Builder}: 
 * <ol>
 * <li>all non-overloaded and distinct overloaded public methods are available as-is
 * <li>overloaded methods with the same number of arguments should be avoided, as the common EL rule
 * <li>the fields are treated similarly to EL map properties, by name or FieldDescriptor as the key
 * <li>the repeated fields, like EL lists, can be accessed by index
 * <li>in addition, Builder's repeated fields have all the additional methods of
 * <li>{@link ProtoLists.IRepeatedFieldMessageBuilder} - for the Message type repeated field:
 * <ol>
 * <li>add() - adds and returns the empty field Builder 
 * <li>add(value) - adds the MessageOrBuilder value and returns the builder
 * <li>add(index, value) - adds the MessageOrBuilder value at index and returns the builder
 * <li>addAll(Collection<? extends T> values) - adds all MessageOrBuilder values and returns the builder 
 * <li>set(index, value) - sets the MessageOrBuilder value at index and returns the builder
 * <li>remove(index) - removes the item at index and returns the builder 
 * <li>clear() - clears the repeated field and returns the builder
 * <li>getParent() - returns this builder's parent, if any 
 * <li>newInstance() - returns a new empty standalone Builder for the field's MessageType
 * <li>size() - returns the size of the field
 * <li>get(index) - returns a field Builder at index
 * <li>getChangedIndex() - returns the last changed item's index
 * <li>getChanged(index) - returns the field Builder at index if it changed recently, or null otherwise 
 * <li>getLast() - returns the last field Builder
 * <li>getList() - returns the entire field's Message list
 * <li>getFieldDescriptor() - returns the field's FieldDescriptor
 * <li>getType() - returns JavaType.Message
 * <li>getBuilders() - returns the entire field's Builder list , for the Builder only
 * <li>getMessage(index) - returns the Message at index
 * </ol>
 * <li>{@link ProtoLists.IRepeatedFieldValueBuilder} - for any other repeated field
 * <ol>
 * <li>add() - adds a default value and returns it
 * <li>add(value) - adds the value and returns the builder
 * <li>add(index, value) - adds the value at index and returns the builder
 * <li>addAll(Collection<? extends T> values) - adds all values and returns the builder 
 * <li>set(index, value) - sets the value at index and returns the builder
 * <li>remove(index) - removes the item at index and returns the builder 
 * <li>clear() - clears the repeated field and returns the builder
 * <li>getParent() - returns this builder's parent, if any 
 * <li>newInstance() - returns a new default value of the field type
 * <li>size() - returns the size of the field
 * <li>get(index) - returns the item at index
 * <li>getChangedIndex() - returns the last changed item's index
 * <li>getChanged(index) - returns the item at index if it changed recently, or null otherwise 
 * <li>getLast() - returns the last item
 * <li>getList() - returns the entire field's list of items
 * <li>getFieldDescriptor() - returns the field's FieldDescriptor
 * <li>getType() - returns the JavaType of the field
 * </ol>
 * </ol>
 * <p>Examples of usage in EL expressions: 
 * <ol>
 * <li>{@code galaxy.name}
 * <li>{@code galaxy['name']}
 * <li>{@code galaxyBuilder.name = 'Silky Way'}
 * <li>{@code galaxyBuilder['name'] = 'E Bay'}
 * <li>{@code galaxy.star} - returns the list of Star(-s) 
 * <li>{@code galaxy.star[1].name} 
 * <li>{@code galaxyBuilder.star} - returns the repeated field Star's special wrapper
 * <li>{@code galaxyBuilder.star[0].name}
 * <li>{@code galaxyBuilder.star.add()} - returns the new empty star added to the Star field
 * <li>{@code sun=galaxyBuilder.star.newInstance(); sun.name='Sun'; galaxyBuilder.star.add(1, sun)
 * .size()}
 * <li>{@code galaxyBuilder.star.remove(1).getLast()}
 * <li>{@code galaxyBuilder.star.remove(1).toParent().color = 'RED'} - galaxy color set to RED
 * <li>{@code galaxyBuilder.star.getBuilders()}
 * <li>{@code galaxyBuilder.star.getList()} - returns list of {@code Star}s
 * <li>{@code galaxyBuilder.keyword[0] == 'my keyword'} 
 * </ol>
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
