FROM arm64v8/debian:bookworm

RUN apt-get update && apt-get install -y build-essential curl file maven

RUN curl -LOs https://github.com/bell-sw/LibericaNIK/releases/download/23.1.3+1-21.0.3+10/bellsoft-liberica-vm-core-openjdk21.0.3+10-23.1.3+1-linux-aarch64.tar.gz && \
  tar xfvz bellsoft-liberica-vm-core-openjdk21.0.3+10-23.1.3+1-linux-aarch64.tar.gz

RUN curl -O https://more.musl.cc/10.2.1/x86_64-linux-muslx32/aarch64-linux-musl-native.tgz && \
  tar xfvz aarch64-linux-musl-native.tgz

RUN curl -O https://zlib.net/zlib-1.3.1.tar.gz && \
  tar xfvz zlib-1.3.1.tar.gz

ENV TOOLCHAIN_DIR=/aarch64-linux-musl-native
ENV CC=$TOOLCHAIN_DIR/bin/gcc
RUN cd zlib-1.3.1 && ./configure --prefix=$TOOLCHAIN_DIR --static && make && make install

COPY . .

ENV JAVA_HOME=/bellsoft-liberica-vm-core-openjdk21-23.1.3
ENV PATH=/bellsoft-liberica-vm-core-openjdk21-23.1.3/bin:$PATH

RUN mvn install -f vizlink/pom.xml -q
RUN mvn package -f vizlink/pom.xml -q -Pnative -DskipTests -Djava.awt.headless=false

# RUN cd vizlink && \
#   javac EnvMap.java && \
#   native-image --static --libc=musl --native-compiler-path=$TOOLCHAIN_DIR/bin/aarch64-linux-musl-gcc EnvMap
