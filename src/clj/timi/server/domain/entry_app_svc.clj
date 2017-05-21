(ns timi.server.domain.entry-app-svc
  (:require
    [timi.server.domain.entry :as entry]
    [timi.server.domain.project :as project]
    [timi.server.domain.task :as task]
    [timi.server.domain.user :as user-repo]
    [timi.server.infra.date-time :refer [local-time? before? local-date?]]))

(defn- valid-task? [task-id]
  (and
    (integer? task-id)
    (task/task-exists? task-id)))

(defn- valid-user? [user-id]
  (and
    (integer? user-id)
    (user-repo/user-exists? user-id)))

(defn post-new-entry!
  [{:keys [task-id user-id] :as cmd}]
  {:pre [(valid-task? task-id)
         (valid-user? user-id)]}
  (let [task (task/get task-id)
        project (project/get (:project-id task))
        entry (entry/new-entry cmd :for-task task :for-project project)]
    (entry/add-entry! entry)))

(defn update-entry!
  [cmd]
  (let [entry (entry/find-entry (:entry-id cmd))]
    (entry/save!
      (entry/update-entry
        entry (assoc cmd
                     :old-task (task/get (:task-id entry))
                     :new-task (task/get (:task-id cmd)))))))

(defn delete-entry!
  [{:keys [entry-id] :as cmd}]
  {:pre [(integer? entry-id)]}
  (let [entry (entry/find-entry entry-id)]
    (assert entry "Entry not found for user")
    (let [entry' (entry/delete-entry entry cmd)]
      (entry/delete-entry! entry'))))
