javac = /opt/jdk-20.0.1/bin/javac

all: build

build:
	$(javac) *.java
	$(javac) -d . UrlDao.java
	$(javac) -d . ThreadWork.java
	$(javac) -d . DatabaseWriteWorker.java
	$(javac) -d . URLShortnerWorker.java
	$(javac) -d . URLShortnerOptimized.java

clean:
	rm *.class