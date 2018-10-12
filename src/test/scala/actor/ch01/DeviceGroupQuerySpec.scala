package actor.ch01

import actor.ch01.iot.{Device, DeviceGroup, DeviceGroupQuery}
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

import scala.concurrent.duration._

class DeviceGroupQuerySpec extends FreeSpec with BeforeAndAfterAll {
  implicit val system = ActorSystem("test")

  "return temperature value for working devices" in {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = system.actorOf(DeviceGroupQuery.props(
      Map(device1.ref -> "device1", device2.ref -> "device2"),
      1,
      requester.ref,
      timeout = 10.seconds
    ))

    device1.expectMsg(Device.ReadTemperature(0))
    device2.expectMsg(Device.ReadTemperature(0))

    queryActor.tell(Device.RespondTemperature(0, Some(1.0)), device1.ref)
    queryActor.tell(Device.RespondTemperature(0, Some(2.0)), device2.ref)

    requester.expectMsg(
      DeviceGroup.RespondAllTemperatures(
        1,
        Map(
          "device1" -> DeviceGroup.Temperature(1.0),
          "device2" -> DeviceGroup.Temperature(2.0)
        )
      )
    )
  }

  "return TemperatureNotAvailable for devices with no readings" in {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = system.actorOf(
      DeviceGroupQuery.props(
        Map(
          device1.ref -> "device1",
          device2.ref -> "device2"
        ),
        1,
        requester.ref,
        3.seconds
      )
    )

    device1.expectMsg(Device.ReadTemperature(0))
    device2.expectMsg(Device.ReadTemperature(0))

    queryActor.tell(Device.RespondTemperature(0, None), device1.ref)
    queryActor.tell(Device.RespondTemperature(0, Some(2.0)), device2.ref)

    requester.expectMsg(
      DeviceGroup.RespondAllTemperatures(
        1,
        Map(
          "device1" -> DeviceGroup.TemperatureNotAvailable,
          "device2" -> DeviceGroup.Temperature(2.0)
        )
      )
    )
  }

  "return DeviceTimedOut if device does not answer in time" in {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = system.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 1.second
    ))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.Temperature(1.0),
        "device2" -> DeviceGroup.DeviceTimedOut
      )
    ))
  }

  override protected def afterAll(): Unit = super.afterAll()
}
