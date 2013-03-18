package de.sciss.desktop
package impl

import java.awt.{Point, Rectangle, Dimension}
import java.io.File
import swing.{RootPanel, Action, Component}
import javax.swing.{JMenuBar, JFrame, JInternalFrame}
import java.awt.event.{ContainerEvent, ContainerListener}

object WindowImpl {
  object Delegate {
    def internalFrame(peer: JInternalFrame, hasMenuBar: Boolean): Delegate =
      new InternalFrame(peer, hasMenuBar = hasMenuBar)

    def frame(peer: swing.Frame, hasMenuBar: Boolean, screen: Boolean): Delegate =
      new Frame(peer, hasMenuBar = hasMenuBar, screen = screen)

    private final class InternalFrame(peer: JInternalFrame, hasMenuBar: Boolean) extends Delegate {
      delegate =>
      val component = new RootPanel { def peer = delegate.peer }

      def closeOperation = Window.CloseOperation(peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation) { peer.setDefaultCloseOperation(value.id) }

      def title = peer.getTitle
      def title_=(value: String) { peer.setTitle(value) }
      def resizable = peer.isResizable
      def resizable_=(value: Boolean) { peer.setResizable(value) }

      def pack() {
        peer.pack()
      }
    }

    private final class Frame(val component: swing.Frame, hasMenuBar: Boolean, screen: Boolean)
      extends Delegate /* with ContainerListener */ {

      def closeOperation = Window.CloseOperation(component.peer.getDefaultCloseOperation)
      def closeOperation_=(value: Window.CloseOperation) { component.peer.setDefaultCloseOperation(value.id) }

      def title = component.title
      def title_=(value: String) { component.title = value }
      def resizable = component.resizable
      def resizable_=(value: Boolean) { component.resizable = value }

      def pack() {
        component.pack()
      }

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
    }
  }
  sealed trait Delegate {
    def component: RootPanel
    var closeOperation: Window.CloseOperation
    var title: String
    var resizable: Boolean
    def pack(): Unit
  }
}
trait WindowImpl extends Window {
  import WindowImpl._

  protected def style: Window.Style

  final protected def application: SwingApplication = handler.application

  final def size = component.size
  final protected def size_=(value: Dimension) { component.peer.setSize(value) }
  final def bounds = component.bounds
  final protected def bounds_=(value: Rectangle) { component.peer.setBounds(value) }
  final def location = component.location
  final def location_=(value: Point) { component.peer.setLocation(value) }
  final def title = delegate.title
  final protected def title_=(value: String) { delegate.title = value }
  final def resizable = delegate.resizable
  final protected def resizable_=(value: Boolean) { delegate.resizable = value }
  final protected def closeOperation = delegate.closeOperation
  final protected def closeOperation_=(value: Window.CloseOperation) { delegate.closeOperation = value }

  final protected def pack() { delegate.pack() }
  final protected def contents = component.contents
  final protected def contents_=(value: Component) { component.contents = value }

  private var _dirty = false
  final protected def dirty: Boolean = _dirty
  final protected def dirty_=(value: Boolean) {
    if (_dirty != value) {
      _dirty = value
      putClientProperty("windowModified", value)
    }
  }

  private def putClientProperty(key: String, value: Any) {
    component.peer.getRootPane.putClientProperty(key, value)
  }

  private final val delegate: Delegate = {
    // XXX TODO
//    style match {
//      case Window.Palette =>
//
//      case _ =>
        if (handler.usesInternalFrames) {
          val jif = new JInternalFrame(null, true, true, true, true)
          // handler.getDesktop().add( jif )
          val hasMenuBar = style == Window.Regular
          Delegate.internalFrame(jif, hasMenuBar = hasMenuBar)

        } else {
          val screen = handler.usesScreenMenuBar
          val f = new swing.Frame
          val hasMenuBar = screen || (style == Window.Regular)
          Delegate.frame(f, screen = screen, hasMenuBar = hasMenuBar)
        }
      //			floating			= false;
      //     			tempFloating		= style == Window.Auxiliary && wh.usesFloating();
      //     			floating			= tempFloating;
//    }
  }

  private var _file = Option.empty[File]
  final def file = _file
  final def file_=(value: Option[File]) {
    _file = value
    putClientProperty("Window.documentFile", value.orNull)
  }

  private var _alpha = 1f
  final def alpha = _alpha
  final def alpha_=(value: Float) {
    _alpha = value
    putClientProperty("Window.alpha", value)
    putClientProperty("apple.awt.draggableWindowBackground", false)
  }

  final protected def makeUnifiedLook() {
    putClientProperty("apple.awt.brushMetalLook", true)
  }

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

  protected def showDialog[A](source: DialogSource[A]): A = {
 		handler.showDialog(this, source)
 	}

  protected def addAction(key: String, action: Action) {
    val a       = action.peer
    val stroke  = action.accelerator.getOrElse(throw new IllegalArgumentException(s"addAction($key, $action) - no accelerator found"))
    val root    = component.peer.getRootPane
    root.registerKeyboardAction(a, key, stroke, FocusType.Window.id)
  }
}

trait MainWindowImpl extends WindowImpl {
  final protected def style = Window.Regular

  handler.setDefaultBorrower(this)
  closeOperation = Window.CloseIgnore
  reactions += {
    case Window.Closing(_) => application.quit()
  }
}