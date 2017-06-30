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

import java.io.File;

import org.antlr.v4.gui.TestRig;

public class GUITestRig1 {
  private static final int ARGS_INPUT_FILE_INDEX = 0;
  private static final int ARGS_GRAMMAR_NAME_INDEX = 1;
  private static final int ARGS_START_RULE_INDEX = 2;

  public static void main(final String[] args) throws Exception {
    String grammarName = "Proto";
    String startRule = "proto";
    String inputFile = "test0.proto";

    if (args.length > ARGS_INPUT_FILE_INDEX) {
      inputFile = args[ARGS_INPUT_FILE_INDEX];
    } else if (args.length > ARGS_GRAMMAR_NAME_INDEX) {
      grammarName = args[ARGS_GRAMMAR_NAME_INDEX];
    } else if (args.length > ARGS_START_RULE_INDEX) {
      startRule = args[ARGS_START_RULE_INDEX];
    }

    final GUITestRig1 testRig = new GUITestRig1();
    final String[] argsuments = testRig.setUp(grammarName, startRule, inputFile);
    TestRig.main(argsuments);
  }

  public String[] setUp(final String grammarName, final String startRule, final String inputFile)
      throws Exception {
    return new String[] {getClass().getPackage().getName() + "." + grammarName, startRule,
        "-tokens", "-tree", "-gui", "-trace", "-diagnostics", "-SLL",
        getResourceFileName(inputFile)};
  }

  private String getResourceFileName(final String resource) {
    final File file = new File(resource);

    try {
      if (file.isAbsolute()) {
        return file.getCanonicalPath();
      } else {
        return getClass().getResource(resource).toURI().toURL().toString();
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
