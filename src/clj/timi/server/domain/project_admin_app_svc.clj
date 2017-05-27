(ns timi.server.domain.project-admin-app-svc
  (:require
    [timi.server.domain.project :as project])
  (:refer-clojure :exclude [get]))

(defn new-project!
  [cmd]
  (-> cmd
      (project/new-project)
      (project/add!)))

(defn get
  []
  (project/get-all))
