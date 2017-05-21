(ns timi.server.web.routes
  (:require
    [compojure.core :refer [GET POST context defroutes routes]]
    [ring.util.mime-type :as mime]
    [ring.util.response :as response]
    [timi.server.web.screens.time :as time-screen]
    [timi.server.web.screens.projects :as projects-screen]
    [timi.server.web.screens.reports :as reports-screen]
    [timi.server.infra.date-time :refer [today format-date str->local-date]]))

(defroutes time-routes
  (context "/time" []
    (GET "/activity-graphic"
         request
         (time-screen/activity-graphic request))
    (GET "/day-entries"
         request
         (time-screen/day-entries-table request))
    (GET "/"
         request
         (response/redirect (str "/time/" (format-date (today)))))
    (GET "/:for-date"
         [for-date :<< str->local-date :as request]
         (time-screen/get-page for-date request))
    (POST "/:for-date"
          request
          (time-screen/post request))))

(defroutes projects-routes
  (GET "/projects"
       request
       (projects-screen/get-page request)))

(defroutes reports-routes
  (GET "/reports"
       request
       (reports-screen/get-page request)))

(defroutes default-routes
  (GET "/*" {{resource-path :*} :route-params}
       (fn [req]
         (let [resp (some->
                      (response/resource-response
                        (str "public/" resource-path))
                      ((fn [resp]
                         (if-let [mime-type (mime/ext-mime-type resource-path)]
                           (response/content-type resp mime-type)
                           resp))))]
           (or
             resp
             (response/not-found
               (str "Could not find " resource-path)))))))

(defroutes all
  (GET "/"
       []
       (response/redirect "/time"))
  (routes time-routes
          projects-routes
          reports-routes
          default-routes))
