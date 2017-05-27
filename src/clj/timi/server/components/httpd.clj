(ns timi.server.components.httpd
  (:require
    [com.stuartsierra.component :as component]
    [org.httpkit.server :as httpkit]
    [taoensso.timbre :as log]))

(defn inject-app
  "Make app components available to request handlers."
  [handler cpnt]
  (fn [request]
    (handler (assoc request :component cpnt))))

(defrecord HTTPServer [app-handler-fn]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [app-cfg (get-in component [:cfg-mgr :cfg])
          http-cfg (:httpd app-cfg)
          handler (inject-app (app-handler-fn app-cfg) component)
          server (httpkit/run-server handler http-cfg)]
      (log/debug "Using config:" http-cfg)
      (log/debug "Component keys:" (keys component))
      (log/debug "Successfully created server:" server)
      (assoc component :httpd server)))

  (stop [component]
    (log/info "Stopping HTTP server ...")
    (log/debug "Component keys" (keys component))
    (if-let [server (:httpd component)]
      (do (log/debug "Using server object:" server)
          (server))) ; calling server like this stops it, if started
    (assoc component :httpd nil)))

(defn new-server [app-handler-fn]
  (->HTTPServer app-handler-fn))
