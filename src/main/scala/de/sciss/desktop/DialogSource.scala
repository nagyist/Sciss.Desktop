/*
 *  DialogSource.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss
package desktop

import java.util.StringTokenizer


object DialogSource {
  implicit final class Dialog(val source: swing.Dialog) extends DialogSource[Unit] {
    def show(window: Option[Window]): Unit = source.open()
  }

  implicit final class Exception(val source: (scala.Exception, String)) extends DialogSource[Unit] {
    private def uncamelizeClassName(x: Any): String = {
      val cn0 = x.getClass.getName
      val i   = cn0.lastIndexOf('.')
      val cn  = cn0.substring(i + 1)
      val len = cn.length
      val b   = new StringBuilder(len + len/2)
      var j   = 0
      var wasUpper = true
      while (j < len) {
        val c       = cn.charAt(j)
        val isUpper = c.isUpper
        if (!wasUpper && isUpper) b.append(' ')
        b.append(c)
        wasUpper    = isUpper
        j += 1
      }
      b.result()
    }

    def show(window: Option[Window]): Unit = {
      val (exception, title) = source
      val name    = if (exception == null) "Exception" else uncamelizeClassName(exception)
      val strBuf  = new StringBuilder(name)
      val message = if (exception == null) "null" else exception.getLocalizedMessage
      var lineLen = 0
      val options = Seq("Ok", "Show Stack Trace")
      val tok     = new StringTokenizer(message)
      strBuf.append(":\n")
      while (tok.hasMoreTokens) {
        val word = tok.nextToken()
        if (lineLen > 0 && lineLen + word.length() > 40) {
          strBuf.append("\n")
          lineLen = 0
        }
        strBuf.append(word)
        strBuf.append(' ')
        lineLen += word.length() + 1
      }
      val op = desktop.OptionPane(message = strBuf.toString(), messageType = desktop.OptionPane.Message.Error,
        optionType = desktop.OptionPane.Options.YesNo, entries = options, initial = Some(options(0)))
      op.title = title

      if (op.show(window).id == 1) {
        exception.printStackTrace()
      }
    }
  }
}
trait DialogSource[+A] {
  def show(window: Option[Window]): A
}