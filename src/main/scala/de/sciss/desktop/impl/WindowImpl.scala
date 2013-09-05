/*
 *  WindowImpl.scala
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

package de.sciss.desktop
package impl

import java.awt.{Point, Rectangle, Dimension}
import java.io.File
import swing.{MenuBar, Reactions, RootPanel, Action, Component}
import javax.swing.JInternalFrame
import java.awt.event.{WindowEvent, WindowListener}
import javax.swing.event.{InternalFrameEvent, InternalFrameListener}

object WindowImpl {
  private[impl] object Delegate {
    def internalFrame(window: Window, peer: JInternalFrame, hasMenuBar: Boolean): Delegate =
      new InternalFrame(window, peer, hasMenuBar = hasMenuBar)

    def frame(window: Window, peer: swing.Frame, hasMenuBar: Boolean, screen: Boolean): Delegate =
      new Frame(window, peer, hasMenuBar = hasMenuBar, screen = screen)

    private final class InternalFrame(window: Window, peer: JInternalFrame, hasMenuBar: Boolean)
      extends Delegate with InternalFrameListener {
      delegate =>

      val component = new RootPanel { def peer = delegate.peer }
      val reactions = new Reactions.Impl

      peer.addInternalFrameListener(this)
      if (hasMenuBar) peer.setJMenuBar(window.handler.menuFactory.create(window).peer)

      def closeOperation = Window.CloseOperation(peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation): Unit = peer.setDefaultCloseOperation(value.id)

      def title = peer.getTitle
      def title_=(value: String): Unit = peer.setTitle(value)
      def resizable = peer.isResizable
      def resizable_=(value: Boolean): Unit = peer.setResizable(value)
      def alwaysOnTop = false // XXX TODO
      def alwaysOnTop_=(value: Boolean): Unit = {
        // XXX TODO
        //        peer.setAlwaysOnTop(value)
      }

      def makeUndecorated(): Unit = {
        // XXX TODO
      }

      def active = peer.isSelected

      def pack(): Unit = peer.pack()

      def dispose(): Unit = {
        peer.dispose()
        if (hasMenuBar) window.handler.menuFactory.destroy(window)
      }

      def front(): Unit = {
        if (!peer.isVisible) peer.setVisible(true)
        peer.toFront()
      }

      //      def menu_=(value: MenuBar) { peer.setJMenuBar(value.peer) }

      def internalFrameOpened     (e: InternalFrameEvent): Unit = reactions(Window.Opened     (window))
      def internalFrameClosing    (e: InternalFrameEvent): Unit = reactions(Window.Closing    (window))
      def internalFrameClosed     (e: InternalFrameEvent): Unit = reactions(Window.Closed     (window))
      def internalFrameIconified  (e: InternalFrameEvent): Unit = reactions(Window.Iconified  (window))
      def internalFrameDeiconified(e: InternalFrameEvent): Unit = reactions(Window.Deiconified(window))
      def internalFrameActivated  (e: InternalFrameEvent): Unit = reactions(Window.Activated  (window))
      def internalFrameDeactivated(e: InternalFrameEvent): Unit = reactions(Window.Deactivated(window))
    }

    private final class Frame(window: Window, val component: swing.Frame, hasMenuBar: Boolean, screen: Boolean)
      extends Delegate with WindowListener {

      private val peer = component.peer
      val reactions = new Reactions.Impl

      peer.addWindowListener(this)
      if (hasMenuBar) component.menuBar = window.handler.menuFactory.create(window)

      def closeOperation = Window.CloseOperation(peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation): Unit = peer.setDefaultCloseOperation(value.id)

      def title = component.title
      def title_=(value: String): Unit = component.title = value
      def resizable = component.resizable
      def resizable_=(value: Boolean): Unit = component.resizable = value
      def alwaysOnTop = peer.isAlwaysOnTop
      def alwaysOnTop_=(value: Boolean): Unit = peer.setAlwaysOnTop(value)

      def makeUndecorated(): Unit = peer.setUndecorated(true)

      def active = peer.isActive

      def pack(): Unit = component.pack()

      def dispose(): Unit = {
        component.dispose()
        if (hasMenuBar) window.handler.menuFactory.destroy(window)
      }

      def front(): Unit = {
        if (!component.visible) component.visible = true
        peer.toFront()
      }

      //      def menu_=(value: MenuBar) { component.menuBar = value }

      // XXX TODO
      //      def componentAdded(e: ContainerEvent) {
      //        checkMenuBar(e)
      //      }
      //
      //      def componentRemoved(e: ContainerEvent) {
      //        checkMenuBar(e)
      //      }

      //      private def checkMenuBar(e: ContainerEvent) {
      //        val mb = e.getContainer.asInstanceOf[JMenuBar]
      //        val curr = component.menuBar
      //        if (mb.getMenuCount == 0) {
      //          if (curr == mb) {
      //            component.menuBar = swing.MenuBar.NoMenuBar
      //          }
      //        } else {
      //          if (curr == null) {
      //            component.peer.setJMenuBar(mb)
      //          }
      //        }
      //        val rp = component.peer.getRootPane
      //        rp.revalidate()
      //        rp.repaint()
      //      }

      def windowOpened      (e: WindowEvent): Unit = reactions(Window.Opened      (window))
      def windowClosing     (e: WindowEvent): Unit = reactions(Window.Closing     (window))
      def windowClosed      (e: WindowEvent): Unit = reactions(Window.Closed      (window))
      def windowIconified   (e: WindowEvent): Unit = reactions(Window.Iconified   (window))
      def windowDeiconified (e: WindowEvent): Unit = reactions(Window.Deiconified (window))
      def windowActivated   (e: WindowEvent): Unit = reactions(Window.Activated   (window))
      def windowDeactivated (e: WindowEvent): Unit = reactions(Window.Deactivated (window))
    }
  }
  private[impl] sealed trait Delegate {
    def component: RootPanel
    var closeOperation: Window.CloseOperation
    var title: String
    var resizable: Boolean
    var alwaysOnTop: Boolean
//    def menu_=(value: MenuBar): Unit
    def active: Boolean
    def pack(): Unit
    def dispose(): Unit
    def front(): Unit
    def makeUndecorated(): Unit
    def reactions: Reactions
  }
}
trait WindowStub extends Window {
  import WindowImpl._

  protected def style: Window.Style

  final protected def application: SwingApplication = handler.application

  final def size = component.size
  final protected def size_=(value: Dimension): Unit = component.peer.setSize(value)
  final def bounds = component.bounds
  final protected def bounds_=(value: Rectangle): Unit = component.peer.setBounds(value)
  final def location = component.location
  final def location_=(value: Point): Unit = component.peer.setLocation(value)
  final def title = delegate.title
  final protected def title_=(value: String): Unit = delegate.title = value
  final def resizable = delegate.resizable
  final protected def resizable_=(value: Boolean): Unit = delegate.resizable = value
  final protected def closeOperation = delegate.closeOperation
  final protected def closeOperation_=(value: Window.CloseOperation): Unit = delegate.closeOperation = value

  final protected def pack(): Unit = delegate.pack()
  final protected def contents = component.contents
  final protected def contents_=(value: Component): Unit = component.contents = value

  final def active: Boolean = delegate.active
  final def alwaysOnTop: Boolean = delegate.alwaysOnTop
  final def alwaysOnTop_=(value: Boolean): Unit = delegate.alwaysOnTop = value
  final def floating: Boolean = false   // XXX TODO
  final def front(): Unit = delegate.front()
  final def reactions: Reactions = delegate.reactions
  final def visible: Boolean = component.visible
  final def visible_=(value: Boolean): Unit = component.visible = value

  private var _dirty = false
  final protected def dirty: Boolean = _dirty
  final protected def dirty_=(value: Boolean): Unit =
    if (_dirty != value) {
      _dirty = value
      putClientProperty("windowModified", value)
    }

  private def putClientProperty(key: String, value: Any): Unit =
    component.peer.getRootPane.putClientProperty(key, value)

  protected def delegate: Delegate

  //  delegate.menu_=(handler.menuFactory.create(this))

  //  private def borrowMenuFrom(that: Window) {
  // 		if( borrowMenuBar && (barBorrower != that) ) {
  // 			if( (bar != null) && (barBorrower != null) ) {
  // 				barBorrower.setJMenuBar( bar );
  // 				bar = null;
  // 			}
  // 			barBorrower = that;
  // 			bar			= barBorrower == null ? null : barBorrower.getJMenuBar();
  // 			if( active ) {
  // 				if( barBorrower != null ) barBorrower.setJMenuBar( null );
  // 				if( jf != null ) {
  // 					jf.setJMenuBar( bar );
  // 				} else if( jif != null ) {
  // 					handler.getMasterFrame().setJMenuBar( bar );
  // 				} else {
  // 					throw new IllegalStateException();
  // 				}
  // 			}
  // 		}
  // 	}

  private var _file = Option.empty[File]
  final def file = _file
  final def file_=(value: Option[File]): Unit = {
    _file = value
    putClientProperty("Window.documentFile", value.orNull)
  }

  private var _alpha = 1f
  final def alpha = _alpha
  final def alpha_=(value: Float): Unit = {
    _alpha = value
    putClientProperty("Window.alpha", value)
    putClientProperty("apple.awt.draggableWindowBackground", false)
  }

  final protected def makeUnifiedLook(): Unit = putClientProperty("apple.awt.brushMetalLook", true)

  final protected def makeUndecorated(): Unit = delegate.makeUndecorated()

  final def component: RootPanel = delegate.component

  //  def insets: Insets = {
  //    if (w != null) {
  //      w.getInsets()
  //    } else if (jif != null) {
  //      jif.getInsets()
  //    } else {
  //      throw new IllegalStateException()
  //    }
  //  }

  def dispose(): Unit = {
    handler.removeWindow(this)
    delegate.dispose()
  }

  final protected def showDialog[A](source: DialogSource[A]): A = {
 		handler.showDialog(Some(this), source)
 	}

  final protected def addAction(key: String, action: Action): Unit = {
    val a       = action.peer
    val stroke  = action.accelerator.getOrElse(throw new IllegalArgumentException(s"addAction($key, $action) - no accelerator found"))
    val root    = component.peer.getRootPane
    root.registerKeyboardAction(a, key, stroke, FocusType.Window.id)
  }

  final protected def addActions(entries: (String, Action)*): Unit = {
    entries.foreach { case (key, action) => addAction(key, action) }
  }

  final protected def bindMenu(path: String, action: Action): Unit = {
    val root  = handler.menuFactory
    root.get(path) match {
      case Some(it: Menu.ItemLike[_]) =>
        val src = it.action
        action.title            = src.title
        action.icon             = src.icon
        action.accelerator      = src.accelerator
        // putNoNullNull(src, a, Action.MNEMONIC_KEY)
        // action.mnemonic         = src.mnemonic
        // action.longDescription  = src.longDescription
        it.bind(this, action)

      case _ => sys.error(s"No menu item for path '$path'")
    }
  }

  final protected def bindMenus(entries: (String, Action)*): Unit =
    entries.foreach { case (key, action) => bindMenu(key, action) }
}
trait WindowImpl extends WindowStub {
  import WindowImpl._

  protected final lazy val delegate: Delegate = {
    val screen = handler.usesScreenMenuBar
    // XXX TODO
    //    style match {
    //      case Window.Palette =>
    //
    //      case _ =>
    val res = if (handler.usesInternalFrames) {
      val jif = new JInternalFrame(null, true, true, true, true)
      // handler.getDesktop().add( jif )
      val hasMenuBar = style == Window.Regular
      Delegate.internalFrame(this, jif, hasMenuBar = hasMenuBar)

    } else {
      val f = new swing.Frame
      val hasMenuBar = screen || (style == Window.Regular)
      Delegate.frame(this, f, screen = screen, hasMenuBar = hasMenuBar)
    }
    //			floating			= false;
    //     			tempFloating		= style == Window.Auxiliary && wh.usesFloating();
    //     			floating			= tempFloating;
    //    }

    // XXX TODO
    //    val borrowMenu = style == Window.Palette && {
    //      handler.usesInternalFrames || (!handler.usesFloatingPalettes && screen)
    //    }
    //
    //    if (borrowMenu) {
    //      borrowMenuFrom(handler.mainWindow)
    //      wh.addBorrowListener(this)
    //    } else if (ownMenuBar) {
    //    }
    res
  }

  handler.addWindow(this)
}