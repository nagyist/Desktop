/*
 *  MacPlatform.scala
 *  (Desktop)
 *
 *  Copyright (c) 2013-2014 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v3+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.desktop.impl

import java.io.File
import com.apple.eio.FileManager
import de.sciss.desktop.{Desktop, Platform}
import de.sciss.model.impl.ModelImpl
import com.apple.eawt
import com.apple.eawt.{OpenFilesHandler, AppHiddenListener, AppForegroundListener}
import com.apple.eawt.AppEvent.{OpenFilesEvent, AppHiddenEvent, AppForegroundEvent}
import scala.collection.JavaConverters
import scala.swing.Image

object MacPlatform extends Platform with ModelImpl[Desktop.Update] {
  override def toString = "MacPlatform"

  private lazy val app = eawt.Application.getApplication

  def revealFile     (file: File): Unit = FileManager revealInFinder file
  def moveFileToTrash(file: File): Unit = FileManager moveToTrash    file

  def setDockBadge(label: Option[String]): Unit = app setDockIconBadge label.orNull
  def setDockImage(image: Image         ): Unit = app setDockIconImage image

  def requestUserAttention (repeat    : Boolean): Unit = app requestUserAttention repeat
  def requestForeground    (allWindows: Boolean): Unit = app requestForeground    allWindows

  private lazy val _init: Unit = init()

  private def init(): Unit = {
    // the following events are fired on the event dispatch thread
    app.addAppEventListener(new AppForegroundListener {
      def appRaisedToForeground(e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationActivated  )
      def appMovedToBackground (e: AppForegroundEvent): Unit = dispatch(Desktop.ApplicationDeactivated)
    })
    app.addAppEventListener(new AppHiddenListener {
      def appUnhidden          (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationShown      )
      def appHidden            (e: AppHiddenEvent    ): Unit = dispatch(Desktop.ApplicationHidden     )
    })
    app.setOpenFileHandler(new OpenFilesHandler {
      def openFiles(e: OpenFilesEvent): Unit = {
        // println(s"openFiles. EDT? ${java.awt.EventQueue.isDispatchThread}")
        import JavaConverters._
        val sq = e.getFiles.asScala.toList
        dispatch(Desktop.OpenFiles(e.getSearchTerm, sq))
      }
    })

    // sys.addShutdownHook {
    //   println("Shutdown")
    // }
  }

  override protected def startListening(): Unit = _init
}