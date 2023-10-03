SRCS = SimpleProxyServer.java SimpleProxyThread.java

all: build execute

build: $(SRCS)
	javac -d . SimpleProxyThread.java
	javac -d . SimpleProxyServer.java

execute:
	java proxy.SimpleProxyServer