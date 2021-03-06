/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_3.codegen.ir

import org.neo4j.cypher.internal.compiler.v2_3.codegen.{Variable, CodeGenContext, MethodStructure}

/**
 * Generates code that runs a loop and afterwards checks if the provided variable has been set,
 * if not it sets all provided variables to null and runs the inner body of the loop
 * @param loop
 * @param yieldedFlagVar
 * @param nullableVars
 */
case class NullingWhileLoop(loop: WhileLoop, yieldedFlagVar: String, nullableVars: Variable*)
  extends Instruction {

  override def init[E](generator: MethodStructure[E])(implicit context: CodeGenContext) =
    loop.init(generator)

  override def body[E](generator: MethodStructure[E])(implicit context: CodeGenContext) = {
    generator.declareFlag(yieldedFlagVar, initialValue = false)
    loop.body(generator)
    generator.ifStatement(generator.not(generator.load(yieldedFlagVar))){ ifBody =>
      //mark variables as null
      nullableVars.foreach(v => ifBody.markAsNull(v.name, v.cypherType))
      loop.action.body(ifBody)
    }
  }

  override def children = Seq(loop)
}
