# ouch-it-hurts

Web application of patients registry. It's able to search, add, update and delete patients information.

## Build and run

Application contains web ui part and server part. Each of them is able to build and run separately.
Emacs `cider` supported.

## run backend for development

`clojure -A:dev`

For starting server:

`(load "dev")` - it loads code from `env/dev/clj/core.clj`

## run frontend for development

`clojure -M -m figwheel.main -b dev -r`
or
`clojure -M:fig:build-front`

REPL and reload are available
See `dev.cljs.edn` config file

## build frontend to `app.js`

`clojure -M -m figweel.main -O advanced -bo prod`
or
`clojure -M:fig:min`

That build `app.js` and put it to target directory.
See `prod.cljs.edn` config file


## build backend to uber-jar

Sure that frontend was built

`clojure -T:build uber`


## run application as uber-jar

Application requires environment variables
- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `PORT`

```
env DB_HOST='localhost' DB_PORT='5432' DB_USER='postgres' DB_PASSWORD='12345678' PORT='8500' java -jar target/app-0.0.1-standalone.jar
```

## build and run docker

Frontend might require path to the server. If docker container run local use `localhost:<container-port>`

`docker build -t ouch-it-hurts-app --build-arg SERVER=<path-to-the-server> .`

set `DB_HOST` `DB_PORT` `DB_USER` `PORT` environment variables through `-e`, `--env` or `--env-file`
when `docker run`

## API

See [API.md](https://github.com/vial0ft/ouch-it-hurts/blob/main/doc/API.md)
