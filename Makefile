.DEFAULT_GOAL := target/vizlink

export JAVA_HOME=$(PWD)/bellsoft-liberica-vm-core-openjdk21-23.1.2/Contents/Home

bellsoft-liberica-vm-core-openjdk21-23.1.2:
	curl -LOs https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz

clean:
	rm -rf target

jdk: bellsoft-liberica-vm-core-openjdk21-23.1.2

prettier:
	pnpm install -D @prettier/plugin-xml prettier prettier-plugin-java
	pnpm prettier -w .

target/vizlink: $(shell find src -type f -name '*.java') jdk
	mvn install -f pom.xml -q
	mvn package -f pom.xml -q -Pnative -DskipTests