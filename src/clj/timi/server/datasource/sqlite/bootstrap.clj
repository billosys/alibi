(ns timi.server.datasource.sqlite.bootstrap
  (:require
    [timi.server.datasource.sqlite.entry-repo :as sqlite-entry-repo]
    [timi.server.datasource.sqlite.project-repo :as sqlite-project-repo]
    [timi.server.datasource.sqlite.queries :as sqlite-queries]
    [timi.server.datasource.sqlite.task-repo :as sqlite-task-repo]
    [timi.server.domain.entry :as entry]
    [timi.server.domain.project :as project]
    [timi.server.domain.query-handler :as queries]
    [timi.server.domain.task :as task]
    [timi.server.domain.user :as user-repo]
    [timi.server.identity.core :as identity]))

(def user-repo
  (reify
    user-repo/UserRepository
    (-user-exists? [_ _] true)
    identity/Identity
    (-user-id-for-username [_ _] 1)))

(defmacro with-sqlite [db-spec & body]
  `(let [db# ~db-spec]
     (entry/with-impl (sqlite-entry-repo/new db#)
       (project/with-repo-impl (sqlite-project-repo/new db#)
         (task/with-impl (sqlite-task-repo/new db#)
           (user-repo/with-impl user-repo
             (identity/with-impl user-repo
               (queries/with-handler (sqlite-queries/handler db#)
                 ~@body))))))))
