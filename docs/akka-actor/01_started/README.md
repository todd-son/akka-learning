# Introduction to the Example

센서 디바이스에서 입력된 데이터를 전송하여 대시보드에 나타내는 예제 프로그램을 짤 것임

두가지 구성요소가 있음

- Device data collection: 원격 장치의 상태 수집기. 하나의 집에 대한 여러개의 센서 장치를 모아서 디바이스 그룹 구성 
- User dashboard: 주기적으로 디바이스 데이터 수집하여 기록하고 리포트함

![IOT Example](/images/01_started/01.png)

# Actor Architecture

![Architecture](/images/01_started/02.png)

아카에서 액터는 항상 부모에 속함. 사실 아카는 시스템상에 세 개의 액터를 미리 만들어 놓고 있음.

- / => 루트 가디언. 모든 액터들의 부모
- /user => 유저가 만드는 모든 액터들의 부모 
- /system => 시스템 관련 액터

system.actorOf()를 이용해서 액터시스템 하위의 액터를 만들 수 있고, actor 내부에서 context.actorOf()를 통해 자식 액터를 생성할 수 있다.

[ActorHierarchyExperiments](/src/main/scala/actor/ch01/ActorHierarchyExperiments.scala)

### Actor Lifecycle

액터가 종료되면 자식 액터들도 재귀적으로 종료된다. context.stop(self)를 통해서 자신을 중지시킬 수 있고, context.stop(actorRef)를 통해서 다른 액터를 종료시킬 수 있다. 그러나 이것은 안좋은 패턴이므로, 커스텀 종료 메시지를 전송하도록 하자.

preStart와 postStop을 라이프사이클 훅으로 제공한다.

[StartStopActorExperiments](/src/main/scala/actor/ch01/StartStopActorExperiments.scala)

### Failure Handling

실패의 디폴트 실패 전략을 Actor를 재실행하는 것이다.

[SupervisingActorTest](/src/main/scala/actor/ch01/SupervisingActorTest.scala)

# IOT System

![IotSystem](/images/03.png)

## IotSupervisor



## Device Actor

### Message Delivery

메시징 서브시스템에서 제공하는 시맨틱은 다음과 같다.

- At-most-once delivery — 각 메시지는 전달되지 않거나 한번만 전달된다. 이 의미는 유실될 수 있으나 절대 중복 전송되지 않는다. 
- At-least-once delivery — 각 메시지는 잠재적으로 한번 성공할때 까지 여러번 전달 될 수 있다. 이 의미는 중복될 수 있으나 절대 유실되지 않는다. 
- Exactly-once delivery — 정확하게 한번 전달 된다. 잃어버리거나 중복되지 않는다.

첫번째 동작을 아카에서 사용하면 비용대비 성능이 가장 좋다. 두번째는 재시도에 의한 전송 비용이 증가할 것 이다. 전송완료 상태와 ack를 관리해야 한다. 세번째는 가장 비싸고 성능이 나오지 않는다. 

대부분의 프레임워크나 프로토콜에서 타켓 액터가 언제 메시지 처리를 시작했는지 혹은 타겟에서 언제 메시지 처리 수행을 완료했는지에 대한 보장을 요구 받는다.
만약 이러한 상황을 극복하기 위해 내부 API가 외부 데이터베이스에 처리 상태를 입력한다고 가정해보자. 이럴때에도 다음과 같은 일이 발생할 수 있다.

- host가 크래쉬된다.
- 역직렬화가 실패할 수 있다.
- 데이터베이스가 크래쉬 될 수 있다.
- 프로그래밍의 오류가 있을 수도 있다.

전송을 보장해달라는 요구 자체가 비현실적일 수 있다. 어디든 크리티컬 섹션은 존재할 수 밖에 없다. 이에 Akka는 어플리케이션 자체에서 이러한 보장을 가져가기를 권고한다.

### Message Ordering

두개의 액터에서 동시에 메시지를 전송하면 메시지 전송의 순서를 보장할 수는 없다.

예를 들어 설명하면 

- Actor A1에서 A2로 메시지 M1, M2, M3를 전송한다.
- Actor A3에서 A2로 메시지 M4, M5, M6를 전송한다.

아카 메시지들의 순서는 다음과 같다.

- M1은 M2, M3 보다는 빨리 도착한다.
- M2는 M3 보다는 빨리 도착한다.
- M4는 M5, M6 보다는 빨리 도착한다.
- M5는 M6 보다는 빨리 도착한다.
- A1과 A3는 메시지 간의 순서는 보장되지 않는다. A2는 각 사이에 메시지를 끼워 넣을수 있다. 





