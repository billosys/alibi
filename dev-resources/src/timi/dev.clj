(ns timi.dev
  "Tímı's REPL development namespace."
  (:require
    [clojure.core.async :as async]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint print-table]]
    [clojure.string :as string]
    [clojure.test :refer :all]
    [clojure.tools.namespace.repl :as repl]
    [clojure.walk :refer [macroexpand-all]]
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.repl-api :as f]
    [taoensso.timbre :as log]
    [timi.server.cli.core :as cli]
    [timi.server.cli.parser :as cli-parser]
    [timi.server.cli.udp :as cli-server]
    [timi.server.components.core :as components]
    [timi.server.core :as timi]
    [timi.server.util :as util]
    [trifl.java :refer [show-methods]]))

(def system nil)
(def state :stopped)

;; Set up default logging here because the system hasn't had a chance to
;; it yet; once the configuration is available, the logger will re-configure.
(logger/set-level! 'timi :info)

(defn init []
  (if (contains? #{:initialized :started :running} state)
    (log/error "System has aready been initialized.")
    (do
      (alter-var-root #'system
        (constantly (timi/get-system)))
      (alter-var-root #'state (fn [_] :initialized))))
  state)


(defn deinit []
  (if (contains? #{:started :running} state)
    (log/error "System is not stopped; please stop before deinitializing.")
    (do
      (alter-var-root #'system (fn [_] nil))
      (alter-var-root #'state (fn [_] :uninitialized))))
  state)

(defn start
  ([]
    (if (nil? system)
      (init))
    (if (contains? #{:started :running} state)
      (log/error "System has already been started.")
      (do
        (alter-var-root #'system component/start)
        (alter-var-root #'state (fn [_] :started))))
    state)
  ([component-key]
    (alter-var-root #'system
                    (constantly (components/start system component-key)))))

(defn stop
  ([]
    (if (= state :stopped)
      (log/error "System already stopped.")
      (do
        (alter-var-root #'system
          (fn [s] (when s (component/stop s))))
        (alter-var-root #'state (fn [_] :stopped))))
    state)
  ([component-key]
    (alter-var-root #'system
                    (constantly (components/stop system component-key)))))

(defn restart [component-key]
  (alter-var-root #'system
                  (constantly (components/restart system component-key))))

(defn run []
  (if (= state :running)
    (log/error "System is already running.")
    (do
      (if (not (contains? #{:initialized :started :running} state))
        (init))
      (if (not= state :started)
        (start))
      (alter-var-root
        #'state (fn [_] :running))))
  state)

(defn -refresh
  ([]
    (repl/refresh))
  ([& args]
    (apply #'repl/refresh args)))

(defn refresh
  "This is essentially an alias for clojure.tools.namespace.repl/refresh."
  [& args]
  (if (contains? #{:started :running} state)
    (stop))
  (apply -refresh args))

(defn reset []
  (stop)
  (deinit)
  (refresh :after 'timi.dev/run))

(defn fig-start
  "This starts the figwheel server and watch based auto-compiler."
  []
  ;; this call will only work are long as your :cljsbuild and
  ;; :figwheel configurations are at the top level of your project.clj
  ;; and are not spread across different lein profiles

  ;; otherwise you can pass a configuration into start-figwheel! manually
  (f/start-figwheel!))

(defn fig-stop
  "Stop the figwheel server and watch based auto-compiler."
  []
  (f/stop-figwheel!))

;; if you are in an nREPL environment you will need to make sure you
;; have setup piggieback for this to work
(defn cljs-repl
  "Launch a ClojureScript REPL that is connected to your build and host environment."
  []
  (f/cljs-repl))
