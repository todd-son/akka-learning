package actor.ch01

import java.util.concurrent.TimeUnit

import actor.ch01.iot.DeviceManager.RequestTrackDevice
import actor.ch01.iot.{Device, DeviceGroup, DeviceManager}
import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestProbe
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DeviceGroupSpec extends FreeSpec with BeforeAndAfterAll {
  private implicit val system = ActorSystem("test")

  "be able to register a device actor" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    val deviceActor1 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    val deviceActor2 = probe.lastSender

    assert(deviceActor1 !== deviceActor2)

    deviceActor1.tell(Device.RecordTemperature(0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(0))
    deviceActor2.tell(Device.RecordTemperature(1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(1))
  }

  "ignore requests for wrong groupId" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device1"), probe.ref)
    probe.expectNoMessage(Duration(500, TimeUnit.MILLISECONDS))
  }

  "return same actor for same deviceId" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    groupActor.tell(RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender

    assert(deviceActor1 === deviceActor2)
  }

  "be able to list active devices" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val toShutDown = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    groupActor.tell(DeviceGroup.RequestDeviceList(0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceList(0, Set("device1", "device2")))

    probe.watch(toShutDown)
    toShutDown ! PoisonPill
    probe.expectTerminated(toShutDown)

    probe.awaitAssert {
      groupActor.tell(DeviceGroup.RequestDeviceList(1), probe.ref)
      probe.expectMsg(DeviceGroup.ReplyDeviceList(1, Set("device2")))
    }

  }

  override def afterAll: Unit = {
    Await.result(system.terminate(), Duration.Inf)
  }
}
