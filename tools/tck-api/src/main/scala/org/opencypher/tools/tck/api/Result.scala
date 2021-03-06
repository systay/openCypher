/*
 * Copyright (c) 2015-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools.tck.api

import org.opencypher.tools.tck.values.CypherValue

import scala.compat.Platform.EOL

/**
  * Convenience implementation for TCK implementers who prefer writing result
  * values in the same string format as the TCK expectations. This is then
  * parsed and converted by the TCK automatically.
  *
  */
case class StringRecords(header: List[String], rows: List[Map[String, String]]) {
  def asValueRecords: CypherValueRecords = {
    val converted = rows.map(_.mapValues(CypherValue(_)))
    CypherValueRecords(header, converted)
  }
}

case class CypherValueRecords(header: List[String], rows: List[Map[String, CypherValue]]) {

  def equalsUnordered(otherRecords: CypherValueRecords): Boolean = {
    def equalHeaders = header == otherRecords.header
    def equalRows = rows.sortBy(_.hashCode()) == otherRecords.rows.sortBy(_.hashCode())
    equalHeaders && equalRows
  }

  override def toString: String = {
    if (header.isEmpty)
      "<empty result>"
    else {
      val _header = header.mkString("| ", " | ", " |")
      val _rows = rows.map(row => {
        val values = header.map(row(_))
        val strings = values.map(_.toString)
        strings.mkString("| ", " | ", " |")
      })
      s"${_header}$EOL${_rows.mkString(s"$EOL")}"
    }
  }
}

object CypherValueRecords {
  def fromRows(header: List[String], data: List[Map[String, String]], orderedLists: Boolean): CypherValueRecords = {
    val parsed = data.map(row => row.mapValues(v => CypherValue(v, orderedLists)).view.force)
    CypherValueRecords(header, parsed)
  }

  val empty = CypherValueRecords(List.empty, List.empty)
  def emptyWithHeader(header: List[String]) = CypherValueRecords(header, List.empty)
}

case class ExecutionFailed(errorType: String, phase: String, detail: String)
