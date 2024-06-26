.DEFAULT_GOAL := vizlink/target/vizlink

export JAVA_HOME=$(PWD)/bellsoft-liberica-vm-core-openjdk21-23.1.2/Contents/Home

bellsoft-liberica-vm-core-openjdk21-23.1.2:
	curl -LOs https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz

clean:
	rm -rf vizlink/target

jdk: bellsoft-liberica-vm-core-openjdk21-23.1.2

jdk-amd64:
	curl -LOs https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-amd64.tar.gz
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-amd64.tar.gz

jdk-windows-amd64:
	curl -LOs https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-windows-amd64.zip
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-windows-amd64.zip
	curl -LOs https://dlcdn.apache.org/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.zip
	unzip apache-maven-3.9.7-bin.zip

prettier:
	pnpm install -D @prettier/plugin-xml prettier prettier-plugin-java
	pnpm prettier -w .

vizlink/target/vizlink: $(shell find vizlink -type f -name '*.java') jdk
	mvn install -f vizlink/pom.xml -q
	mvn package -f vizlink/pom.xml -q -Pnative -DskipTests