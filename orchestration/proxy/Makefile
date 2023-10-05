SRCS = SimpleProxyServer.java SimpleProxyThread.java

all: build execute

build: $(SRCS)
	javac -d . Cache.java
	javac -d . HttpUtil.java
	javac -d . StreamUtil.java
	javac -d . SimpleProxyThread.java
	javac -d . SimpleProxyServer.java

execute:
	java proxy.SimpleProxyServer