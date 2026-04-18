# hlquery Java API Makefile

JAVAC = javac
JAVA = java
JAR = jar
LIB_DIR = lib
SRC_DIR = src/main/java
BIN_DIR = bin
JSON_JAR = $(LIB_DIR)/json.jar
CLASSPATH = $(JSON_JAR):$(BIN_DIR)

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

all: compile

$(JSON_JAR):
	mkdir -p $(LIB_DIR)
	curl -L -o $(JSON_JAR) https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar

compile: $(JSON_JAR)
	mkdir -p $(BIN_DIR)
	$(JAVAC) -cp $(JSON_JAR) -d $(BIN_DIR) $(SOURCES)

example: compile
	$(JAVA) -cp $(CLASSPATH) hlquery.Example

examples-cols: compile
	$(JAVA) -cp $(CLASSPATH) hlquery.examples.Collections

examples-docs: compile
	$(JAVA) -cp $(CLASSPATH) hlquery.examples.Documents

examples-search: compile
	$(JAVA) -cp $(CLASSPATH) hlquery.examples.Search

examples-sql: compile
	$(JAVA) -cp $(CLASSPATH) hlquery.examples.SQL

test: compile
	$(JAVAC) -cp $(CLASSPATH) -d $(BIN_DIR) ../tests/ApiTest.java
	$(JAVA) -cp $(CLASSPATH) ApiTest

clean:
	rm -rf $(BIN_DIR)

.PHONY: all compile example test clean
