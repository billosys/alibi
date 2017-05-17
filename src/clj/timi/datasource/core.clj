(ns timi.datasource.core
  (:require
    [timi.cli :refer [cli-main cli-clj]]
    [timi.datasource.sqlite.bootstrap :refer [with-sqlite]]
    [timi.infra.jdbc-extensions]))

(defn load-persistence-strategy
  [config strategy middlewares-map]
  (let [[strategy-key strategy-config] strategy
        {:keys [requires bootstrap]} strategy-config]
    (when (seq requires)
      (apply require requires))
    (assert bootstrap (str strategy-key " should define a :bootstrap "
                           "function in config"))
    (let [bootstrap-fn (resolve bootstrap)]
      (assert bootstrap-fn (str "Couldn't resolve :bootstrap="
                                bootstrap " to a var, "
                                "are you sure the function exists?"))
      (assoc middlewares-map strategy-key (bootstrap-fn strategy-config)))))

(defn get-persistence-middlewares
  [config]
  (loop [strategies (:persistence-strategies config)
         middlewares {:sqlite (fn [f] (with-sqlite (:sqlite config) (f)))}]
    (if (seq strategies)
      (recur (rest strategies)
             (load-persistence-strategy
               config
               (first strategies)
               middlewares))
      middlewares)))

(defn get-persistence-middleware
  [config]
  (let [persistence-key (:persistence config :sqlite)
        persistence-middlewares (get-persistence-middlewares config)]
    (if-let [middleware (get persistence-middlewares persistence-key)]
      middleware
      (throw (Exception. (str "Persistence strategy " persistence-key
                              " not supported, try one of "
                              (keys persistence-middlewares)))))))

(defn wrap-persistence
  [handler config]
  (let [middleware (get-persistence-middleware config)]
    (fn [req]
      (middleware
        (fn [] (handler req))))))
