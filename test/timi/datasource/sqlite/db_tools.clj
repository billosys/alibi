(ns timi.datasource.sqlite.db-tools
  (:require
    [timi.db-tools :as db-tools]
    [timi.domain.project :as project]
    [timi.domain.task :as task]
    [timi.domain.entry :as entry]
    [timi.domain.user :as user-repo]
    [timi.infra.date-time
     :refer [->local-time ->local-date today]]
    [clojure.java.jdbc :as db]))

(def users (atom []))

(defn sqlite-user-repo []
  (reify user-repo/UserRepository
    (-user-exists? [this user-id] (some (partial = user-id) (map :id @users)))))

(defn clean-all! [db-spec]
  (reset! users [])
  (doseq [table ["entries" "tasks" "projects"]]
    (db/execute! db-spec (str "delete from " table))))

(defn get-default-project-id [db-spec]
  (->
    (db/query db-spec ["select * from projects limit 1"])
    first
    :id))

(defn get-project-id [db-spec project-name]
  (->
    (db/query db-spec
              ["select id from projects where name=?" project-name])
    first
    :id))

(defn get-task-id [db-spec project task-name]
  (cond
    (string? project) (recur db-spec (get-project-id db-spec project)
                             task-name)

    :else
    (->
      (db/query db-spec ["select id from tasks where project_id=? and name=?"
                         project task-name])
      first
      :id)))

(defn bill-entry! [db-spec entry-id]
  (let [entry (entry/find-entry entry-id)]
    (entry/save! (assoc entry :billed? true))))

(defn get-default-task-id [db-spec]
  (-> (db/query db-spec "select id from tasks limit 1")
      first
      :id))

(defn new-user! [user]
  (swap! users (fn [users]
                 (conj users (assoc user :id (inc (count users))))))
  (-> @users last :id))

(defn sqlite-db-tools [db-spec]
  (reify db-tools/DBTools
    (-clean-all! [this] (clean-all! db-spec))
    (-get-project-id [this project-name] (get-project-id db-spec project-name))
    (-new-user! [this user] (new-user! user))
    (-get-task-id [this project-id task-name]
      (get-task-id db-spec project-id task-name))
    (-get-default-user-id [this] (-> @users first :id))
    (-bill-entry! [this entry-id] (bill-entry! db-spec entry-id))
    (-get-default-task-id [this] (get-default-task-id db-spec))
    (-get-default-project-id [this] (get-default-project-id db-spec))))
