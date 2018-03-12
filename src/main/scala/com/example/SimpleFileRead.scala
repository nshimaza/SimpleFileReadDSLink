/**
 *
 * Copyright (c) 2016,2018 Cisco and/or its affiliates.
 *
 * This software is licensed to you under the terms of the Cisco Sample
 * Code License, Version 1.0 (the "License"). You may obtain a copy of the
 * License at
 *
 *                https://developer.cisco.com/docs/licenses
 *
 * All use of the material herein must be in accordance with the terms of
 * the License. All rights not expressly granted by the License are
 * reserved. Unless required by applicable law or agreed to separately in
 * writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */

package com.example

import java.util.concurrent.SynchronousQueue

import org.dsa.iot.dslink.node.value.{Value, ValueType}
import org.dsa.iot.dslink.{DSLink, DSLinkFactory, DSLinkHandler}
import org.slf4j.LoggerFactory

import scala.io.Source

/**
  * Created by nshimaza on 2016/09/28.
  */
object SimpleFileRead {
  def main(args: Array[String]): Unit = {
    val content = Source.fromFile(args(0)).mkString
    val finishMarker = new SynchronousQueue[Unit]()

    val provider = DSLinkFactory.generate(args.drop(1),
      new SimpleFileReadDSLinkHandler(content, () => finishMarker.put(())))

    provider.start()
    finishMarker.take()
    Thread.sleep(1000)
    System.exit(0)
  }
}


class SimpleFileReadDSLinkHandler(content: String, markFinished: () => Unit) extends DSLinkHandler {
  private val log = LoggerFactory.getLogger(getClass)
  override val isResponder = true

  override def onResponderInitialized(link: DSLink): Unit = log.info("SimpleFileReadDSLink initialized")

  override def onResponderConnected(link: DSLink): Unit = {
    log.info("SimpleFileReadDSLink connected")

    val node = link.getNodeManager.getSuperRoot
      .createChild("file", true)
      .setDisplayName("File Content")
      .setValueType(ValueType.STRING)
      .setValue(new Value(""))
      .build

    node.setValue(new Value(content))

    markFinished()
  }
}
