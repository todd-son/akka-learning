package actor.ch01

import java.util.concurrent.TimeUnit

import actor.ch01.iot.{Device, DeviceManager}
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DeviceSpec extends FreeSpec with BeforeAndAfterAll {
  private implicit val system = ActorSystem("test")

  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)

    val response = probe.expectMsgType[Device.RespondTemperature]
    assert(response.requestId === 42)
    assert(response.value === None)
  }

  "reply with latest temperature reading" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.RecordTemperature(1, 24.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(1))

    deviceActor.tell(Device.ReadTemperature(2), probe.ref)
    val response1 = probe.expectMsgType[Device.RespondTemperature]
    assert(response1.requestId === 2)
    assert(response1.value === Some(24.0))

    deviceActor.tell(Device.RecordTemperature(3, 55.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(3))

    deviceActor.tell(Device.ReadTemperature(4), probe.ref)
    val response2 = probe.expectMsgType[Device.RespondTemperature]
    assert(response2.requestId === 4)
    assert(response2.value === Some(55.0))
  }

  "reply to registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "device"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    assert(probe.lastSender === deviceActor)
  }

  "ignore wrong registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.ref)
    probe.expectNoMessage(Duration(500, TimeUnit.MILLISECONDS))

    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.ref)
    probe.expectNoMessage(Duration(500, TimeUnit.MILLISECONDS))
  }

  override def afterAll: Unit = {
    Await.result(system.terminate(), Duration.Inf)
  }
}
