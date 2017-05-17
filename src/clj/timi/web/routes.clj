(ns timi.web.routes
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.mime-type :as mime]
    [ring.util.response :as response]
    [timi.web.screens.time :as time-screen]
    [timi.web.screens.projects :as projects-screen]
    [timi.infra.date-time :refer [today format-date str->local-date]]))

(defroutes all
  (GET "/"
       []
       (response/redirect "/time"))
  (GET "/activity-graphic"
       request
       (time-screen/activity-graphic request))
  (GET "/time/day-entries"
       request
       (time-screen/day-entries-table request))
  (GET "/time"
       request
       (response/redirect (str "/time/" (format-date (today)))))
  (GET "/time/:for-date"
       [for-date :<< str->local-date :as request]
       (time-screen/get-page for-date request))
  (POST "/time/:for-date"
        request
        (time-screen/post request))
  (GET "/projects"
       request
       (projects-screen/get-page request))
  (GET "/*" {{resource-path :*} :route-params}
       (fn [req]
         (let [resp (some->
                      (response/resource-response (str "public/" resource-path))
                      ((fn [resp]
                         (if-let [mime-type (mime/ext-mime-type resource-path)]
                           (response/content-type resp mime-type)
                           resp))))]
           (if resp
             resp
             (response/not-found (str "Could not find " resource-path)))))))
