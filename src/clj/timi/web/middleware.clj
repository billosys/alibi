(ns timi.web.middleware
  (:require
    [ring.middleware.session :as session-middleware]
    [ring.middleware.session.cookie :as cookie-session]
    [ring.util.response :as response]
    [selmer.parser :as selmer]))

(defn wrap-no-cache
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (response/header "Expires" "Thu, 01 Dec 1994 16:00:00 GMT")
          (response/header "Cache-control" "max-age=0")))))

(defn wrap-selmer
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if-let [selmer-template (:selmer-template (:body response))]
        (assoc response :body
               (selmer/render-file selmer-template
                                   (get-in response [:body :template-data])))
        response))))

(defn wrap-default-responses
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (response/response? response)
        (-> response
            (response/content-type
              (get-in response [:headers "Content-Type"] "text/html"))
            (response/charset "utf-8"))
        response))))

(defn wrap-session
  [handler config]
  (session-middleware/wrap-session
    handler
    {:store (cookie-session/cookie-store
              {:key (:cookie-encryption-key config)})}))
