(ns timi.domain.task-admin-app-svc
  (:require
    [timi.domain.project :as project]
    [timi.domain.task :as task]))

(defn new-task!
  [{:keys [for-project-id] :as cmd}]
  {:pre [(project/exists? for-project-id)]}
  (let [task (task/new-task cmd)]
    (task/add! task)))
