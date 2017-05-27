(ns timi.server.components.httpd
  (:require
    [com.stuartsierra.component :as component]
    [org.httpkit.server :as httpkit]
    [taoensso.timbre :as log]))

(defn inject-app
  "Make app components available to request handlers."
  [handler cpnt]
  (fn [request]
    (handler (assoc request :system cpnt))))

(defrecord HTTPServer [ring-handler]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [http-cfg (get-in component [:cfg-mgr :cfg :httpd])
          handler (inject-app ring-handler component)
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

(defn new-server [ring-handler]
  (->HTTPServer ring-handler))
