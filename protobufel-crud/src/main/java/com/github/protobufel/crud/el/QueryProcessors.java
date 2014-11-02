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

package com.github.protobufel.crud.el;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

/**
 * Query Processor Interfaces.
 * 
 * @author protobufel@gmail.com David Tesler
 */
public class QueryProcessors {

  /**
   * Defines a basic search capability.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public interface ISearchable<E> {
    /**
     * Given the query returns a resulted list of elements of type E.
     */
    List<E> search(String query);
  }

  /**
   * Defines a basic query processor.
   * 
   * @author protobufel@gmail.com David Tesler
   */
  public interface IQueryProcessor<E> {
    /**
     * Processes the original elements into the output elements.
     */
    List<E> process(final List<? extends E> originalList);

    /**
     * Processes the original elements into the output elements, with the the provided
     * beans cache.
     */
    List<E> process(final List<? extends E> originalList, Map<String, Object> beans);
  }

  /**
   * An EventListener reporting each single result.
   *  
   * @author protobufel@gmail.com David Tesler
   */
  public interface QueryResultListener extends EventListener {
    /**
     * Notifies when the record added.
     * 
     * @param originalRecordIndex record index
     * @return true - to continue processing, false - to abort
     */
    boolean resultAdded(int originalRecordIndex);
  }

  /**
   * A validation EventListener. 
   * @author protobufel@gmail.com David Tesler
   */
  public interface ValidationListener extends EventListener {
    /**
     * Returns true if validation is succcess, otherwise the processing needs to be aborted. 
     */
    boolean validate(Descriptor type, Message original, MessageOrBuilder result);
  }
}
