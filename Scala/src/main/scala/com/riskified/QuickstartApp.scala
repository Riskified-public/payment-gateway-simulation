package com.riskified

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object QuickstartApp {

  private def startHttpServer(
    routes: Route,
    system: akka.actor.typed.ActorSystem[_]
  )(implicit ec: ExecutionContext): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic

    val futureBinding = Http().bindAndHandle(routes, "localhost", 8000)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val classicSystem: akka.actor.ActorSystem =
        context.system.toClassic
      implicit val executionContext: ExecutionContext =
        context.system.executionContext

      startHttpServer(
        new Healthchecker(new HttpClient(), context.system).route,
        context.system
      )

      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}
