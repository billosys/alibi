(ns timi.core
  (:require
    [timi.config :as config]
    [timi.web.core :as web]
    [timi.cli :refer [cli-main cli-clj]]
    [timi.datasource.core :as datasource]
    [timi.infra.jdbc-extensions]))

(def config (timi.config/read-config))

(def app
  "Used by the ring handler configuration in project.clj."
  (web/app config))

(defn -main [& args]
  (let [persistence-middleware (datasource/get-persistence-middleware config)]
    (persistence-middleware (fn [] (apply cli-main args)))))

(defmacro cli [& args]
  `(-main ~@(map str args)))
