FROM clojure:temurin-20-tools-deps-jammy AS build

ARG SERVER_PORT=""
ARG SERVER_HOST=""

ENV CLOJURE_VERSION=1.11.1.1182

RUN curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install \
    && chmod +x install \
    && ./install \
    && rm install

RUN mkdir -p /build
WORKDIR /build

COPY . /build

RUN echo "Server info: port $SERVER_PORT host $SERVER_HOST"

RUN bb -f make_prod_config.clj -t prod.template.cljs.edn -o prod.cljs.edn -p $SERVER_PORT -H $SERVER_HOST

RUN cat prod.cljs.edn | echo

RUN clojure -M:fig:min

RUN clojure -T:build uber

FROM eclipse-temurin:20-jre-alpine

RUN addgroup -S vial0ft && adduser -S vial0ft -G vial0ft
RUN mkdir -p /service && chown -R vial0ft /service
USER vial0ft

WORKDIR /service

COPY --from=build /build/target/app-0.0.1-standalone.jar /service/app-0.0.1-standalone.jar

EXPOSE $PORT

CMD ["java", "-jar", "/service/app-0.0.1-standalone.jar"]
