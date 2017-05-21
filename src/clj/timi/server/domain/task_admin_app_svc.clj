(ns timi.server.domain.task-admin-app-svc
  (:require
    [timi.server.domain.project :as project]
    [timi.server.domain.task :as task]))

(defn new-task!
  [{:keys [for-project-id] :as cmd}]
  {:pre [(project/exists? for-project-id)]}
  (let [task (task/new-task cmd)]
    (task/add! task)))
