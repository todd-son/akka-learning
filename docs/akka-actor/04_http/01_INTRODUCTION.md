## Introduction 

Akka HTTP는 akka-actor와 akka-stream을 기반한 풀 서버 와 클라이언트 사이트의 HTTP 스택을 구현한 모듈. 웹프레임워크는 아님. HTTP 기반의 서비스를 제공하는데 더욱 일반적인 툴킷이다.

### Philosophy

Akka HTTP 어플리케이션 핵심 영역을 다루기보단 인티그레션 레이어를 작성하는데 필요한 툴을 제공한다. 그래서 프레임워크보다는 라이브러리로 취급한다.

프레임워크는 어플리케이션을 구축하는데 어떤 프레임을 제공하고 이미 제공된 결정과 기초하여 빠른 결과를 제공하는데 초점을 맞춘다. 

그러나 Akka HTTP는 그렇지 않다. 자바스크립트를 렌더링하고 뷰의 템플릿을 제공하거나 하지 않는다.

### Using Akka HTTP

[WebServerSimple](/src/main/scala/http/ch01/WebServerSimple.scala)

[WebServerJson](/src/main/scala/http/ch01/WebServerJson.scala)

[WebServerStream](/src/main/scala/http/ch01/WebServerStream.scala)

[WebServerActor](/src/main/scala/http/ch01/WebServerActor.scala)

### Low-level HTTP Server APIs

[WebServerLowLevel](/src/main/scala/http/ch01/WebServerLowLevel.scala)

### Http client API

[HttpClientSimple](/src/main/scala/http/ch01/HttpClientSimple.scala)