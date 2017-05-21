(ns timi.server.domain.project-admin-app-svc
  (:require
    [timi.server.domain.project :as project]))

(defn new-project! [cmd]
  (let [project (project/new-project cmd)]
    (project/add! project)))
