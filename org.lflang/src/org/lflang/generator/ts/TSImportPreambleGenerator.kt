/*************
 * Copyright (c) 2019-2020, The University of California at Berkeley.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***************/

package org.lflang.generator.ts

import org.lflang.generator.PrependOperator
import org.lflang.joinWithLn
import java.nio.file.Path
import org.lflang.lf.Preamble

/**
 * Preamble generator for imports in TypeScript target.
 *
 *  @author{Matt Weber <matt.weber@berkeley.edu>}
 *  @author{Edward A. Lee <eal@berkeley.edu>}
 *  @author{Marten Lohstroh <marten@berkeley.edu>}
 *  @author {Christian Menard <christian.menard@tu-dresden.de>}
 *  @author {Hokeun Kim <hokeunkim@berkeley.edu>}
 */

class TSImportPreambleGenerator(
    private val filePath: Path,
    private val protoFiles: MutableList<String>,
    private val preambles: List<Preamble>
) {
    companion object {
        /**
         * Default imports for importing all the core classes and helper classes
         * for CLI argument handling.
         */
        const val DEFAULT_IMPORTS = """
            import commandLineArgs from 'command-line-args'
            import commandLineUsage from 'command-line-usage'
            import {Parameter as __Parameter, Timer as __Timer, Reactor as __Reactor, App as __App} from '@lf-lang/reactor-ts'
            import {Action as __Action, Startup as __Startup, FederatePortAction as __FederatePortAction} from '@lf-lang/reactor-ts'
            import {Bank as __Bank} from '@lf-lang/reactor-ts'
            import {FederatedApp as __FederatedApp, FederateConfig as __FederateConfig} from '@lf-lang/reactor-ts'
            import {InPort as __InPort, OutPort as __OutPort, Port as __Port, WritablePort as __WritablePort, WritableMultiPort as __WritableMultiPort} from '@lf-lang/reactor-ts'
            import {InMultiPort as __InMultiPort, OutMultiPort as __OutMultiPort} from '@lf-lang/reactor-ts'
            import {Reaction as __Reaction} from '@lf-lang/reactor-ts'
            import {State as __State} from '@lf-lang/reactor-ts'
            import {TimeUnit, TimeValue, Tag as __Tag, Origin as __Origin} from '@lf-lang/reactor-ts'
            import {Args as __Args, Variable as __Variable, Triggers as __Triggers, Present, Read, Write, ReadWrite, MultiReadWrite, Sched} from '@lf-lang/reactor-ts'
            import {Log} from '@lf-lang/reactor-ts'
            import {ProcessedCommandLineArgs as __ProcessedCommandLineArgs, CommandLineOptionDefs as __CommandLineOptionDefs, CommandLineUsageDefs as __CommandLineUsageDefs, CommandLineOptionSpec as __CommandLineOptionSpec, unitBasedTimeValueCLAType as __unitBasedTimeValueCLAType, booleanCLAType as __booleanCLAType} from '@lf-lang/reactor-ts'
            """
    }

    private fun generateDefaultImports(): String {
        // TODO(hokeun): Generate defaultFederateConfig after $DEFAULT_IMPORTS.

        for (preamble in preambles) {
            preamble.code.body
        }
        return with(PrependOperator) {
            """
            |// Code generated by the Lingua Franca compiler from:
            |// file:/$filePath
${"         |"..DEFAULT_IMPORTS}
            |
            """.trimMargin()
        }
    }

    private fun generateProtoPreamble(): String {
        val protoFileImports = protoFiles.joinWithLn { file ->
            // Remove any extension the file name may have.
            val name = file.substringBeforeLast('.')
            "import * as $name from \"./${name}_pb\""
        }
        return with(PrependOperator) {
                """
                |// Imports for protocol buffers
${"             |"..protoFileImports}
                |
                |
                """.trimMargin()
        }
    }

    fun generatePreamble(): String {
        return generateDefaultImports() + generateProtoPreamble()
    }
}
