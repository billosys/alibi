(ns timi.domain.task-admin-app-svc
  (:require
    [timi.domain.task :as task]
    [timi.domain.project :as project]))

(defn new-task!
  [{:keys [for-project-id] :as cmd}]
  {:pre [(project/exists? for-project-id)]}
  (let [task (task/new-task cmd)]
    (task/add! task)))
