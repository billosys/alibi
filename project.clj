(defproject timi "0.2.0-SNAPSHOT"
  :description "A Clojure(Script)-based time tracker"
  :url "https://github.com/billosys/timi"
  :license {
    :name "Mozilla Public License Version 2.0"
    :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :min-lein-version "2.7.1"
  :dependencies [
    [bouncer "1.0.1" :exclusions [com.andrewmcveigh/cljs-time]]
    [buddy/buddy-auth "1.4.1"]
    [buddy/buddy-sign "1.5.0"]
    [clj-http "2.1.0"]
    [clj-time "0.13.0"]
    [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
    [clojusc/trifl "0.1.0-SNAPSHOT"]
    [clojusc/twig "0.3.2-SNAPSHOT"]
    [com.andrewmcveigh/cljs-time "0.4.0"]
    [com.stuartsierra/component "0.3.2"]
    [compojure "1.6.0"]
    [crypto-random "1.2.0"]
    [http-kit "2.2.0"]
    [mysql/mysql-connector-java "6.0.6"]
    [org.clojure/clojure "1.8.0"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/java.jdbc "0.7.0-alpha2"]
    [org.webjars/bootstrap-datepicker "1.6.4"]
    [org.webjars/bootswatch-superhero "3.3.7"]
    [org.webjars/font-awesome "4.7.0"]
    [org.webjars/jquery "3.2.0"]
    [org.xerial/sqlite-jdbc "3.16.1"]
    [ring-logger "0.7.7"]
    [ring-webjars "0.2.0"]
    [ring.middleware.conditional "0.2.0"]
    [ring/ring-codec "1.0.1"]
    [ring/ring-core "1.6.1"]
    [selmer "1.10.7"]]
  :repl-options {
    :init-ns user}
  :plugins [
    [cider/cider-nrepl "0.10.0"]
    [lein-cljsbuild "1.1.4" :exclusions [[org.clojure/clojure]]]
    [lein-figwheel "0.5.8"]
    [lein-pprint "1.1.1"]
    [lein-ring "0.9.7"]]
  :main timi.core
  :cljsbuild {
    :builds
      [{:id "dev"
        :source-paths ["src/cljs"]
        :figwheel {}
        :compiler {
          :main timi.core
          :asset-path "/dist/cljs/out"
          :output-to "resources/public/dist/cljs/timi.js"
          :output-dir "resources/public/dist/cljs/out"
          :source-map-timestamp true
          :language-in :ecmascript5
          :preloads [devtools.preload]}}
       {:id "single-file"
        :source-paths ["src/cljs"]
        :compiler {
          :output-to "resources/public/dist/cljs/timi.js"
          :main timi.core
          :output-dir "resource/public/dist/cljs/out-single-file"
          :optimizations :simple
          :language-in :ecmascript5
          :pretty-print true}}
       {:id "min"
        :source-paths ["src/cljs"]
        :compiler {
          :output-to "resources/public/dist/cljs/timi.js"
          :main timi.core
          :optimizations :advanced
          :externs [
            "externs/js-joda.js"
            "externs/datepicker.js"
            "externs/selectize.js"]
          ;:pseudo-names true
          :verbose true
          :language-in :ecmascript5
          :pretty-print false}}]}
  :figwheel {
    :server-port 5076
    :css-dirs ["resources/public/res/css"]
    :server-logfile "var/logs/figwheel.log"}
  :profiles {
    :uberjar {
      :aot :all}
    :dev [{
      :dependencies [
        [binaryage/devtools "0.9.4"]
        [cljsjs/react "15.5.0-0"]
        [com.cemerick/piggieback "0.2.1"]
        [figwheel-sidecar "0.5.10"]
        [org.clojure/clojurescript "1.9.542"]
        [org.clojure/core.async "0.3.442"
         :exclusions [org.clojure/tools.reader]]
        [org.omcljs/om "0.9.0"
         :exclusions [cljsjs/react]]
        [ring/ring-mock "0.3.0"]]
      :source-paths [
        "src/clj"
        "src/cljs"
        "dev-resources/src"]
      :repl-options {
        :init (set! *print-length* 50)
        :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
      :resource-paths ["config/dev"]}
     :custom-persistence]
    :test [{
      :resource-paths ["config/test"]
      :plugins [
        [jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]
        [lein-kibit "0.1.5" :exclusions [org.clojure/clojure]]
        [lein-ancient "0.6.10"]]}
     :custom-persistence]
    :local [{:resource-paths ["config/local"]}
            :custom-persistence]
    :demo [{:resource-paths ["config/demo"]}
           :custom-persistence]
    :prod [{:resource-paths ["config/prod"]}
           :custom-persistence]
    :custom-persistence {}}
  :aliases {
    "timi-init" ["run" "sqlite" "create-db" :filename]
    "timi-create-project" ["with-profile" "+local" "run" "projects" "create" :name]
    "timi-create-task" ["with-profile" "+local" "run" "tasks" "create" :name]
    "timi-run" ["with-profile" "+local" "run"]
    "check-deps" ["with-profile" "+test" "ancient" "check" "all"]})
