package actor.ch01.iot

import actor.ch01.iot.Device.{ReadTemperature, RecordTemperature, RespondTemperature, TemperatureRecorded}
import akka.actor.{Actor, ActorLogging, Props}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {
  var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started.", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stopped.", groupId, deviceId)

  override def receive: Receive = {
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceManager.DeviceRegistered


    case DeviceManager.RequestTrackDevice(groupId, deviceId) =>
      log.warning("Ignoring TrackDevice request for {}-{}. The actor is responsible for {}-{}.", groupId, deviceId, this.groupId, this.deviceId)


    case RecordTemperature(id, value) =>
      log.info("Recorded temperature reading {} with {}", value, id)
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(id)

    case ReadTemperature(id) => sender() ! RespondTemperature(id, lastTemperatureReading)
  }
}

object Device{
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  case class ReadTemperature(requestId: Long)
  case class RespondTemperature(requestId: Long, value: Option[Double])

  case class RecordTemperature(requestId: Long, value: Double)
  case class TemperatureRecorded(requestId: Long)
}
