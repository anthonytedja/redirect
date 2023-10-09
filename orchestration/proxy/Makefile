SRCS = SimpleProxyServer.java SimpleProxyThread.java

all: build

build: $(SRCS)
	javac *.java
	javac -d . HostPool.java
	javac -d . SimpleHostPool.java
	javac -d . ConsistentRingPool.java
	javac -d . ThreadWork.java
	javac -d . ParsedHttpRequest.java
	javac -d . ParsedHttpResponse.java
	javac -d . HttpUtil.java
	javac -d . StreamUtil.java
	javac -d . SimpleProxyThread.java
	javac -d . SimpleProxyServer.java
