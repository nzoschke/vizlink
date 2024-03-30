.DEFAULT_GOAL := vizlink/target/vizlink

export JAVA_HOME=$(PWD)/bellsoft-liberica-vm-core-openjdk21-23.1.2/Contents/Home

bellsoft-liberica-vm-core-openjdk21-23.1.2:
	curl -LOs https://download.bell-sw.com/vm/23.1.2/bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz
	tar xfz bellsoft-liberica-vm-core-openjdk21.0.2+14-23.1.2+1-macos-aarch64.tar.gz

clean:
	rm -rf vizlink/target

jdk: bellsoft-liberica-vm-core-openjdk21-23.1.2

prettier:
	pnpm install -D @prettier/plugin-xml prettier prettier-plugin-java
	pnpm prettier -w .

vizlink/target/vizlink: $(shell find vizlink -type f -name '*.java') jdk
	mvn install -f vizlink/pom.xml -q
	mvn -f vizlink/pom.xml -Pnative -DskipTests package