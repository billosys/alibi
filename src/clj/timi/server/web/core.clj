(ns timi.server.web.core
  (:require
    [ring.logger :as logger-middleware]
    [ring.middleware.conditional :refer [if-url-doesnt-match]]
    [ring.middleware.params :as params-middleware]
    [ring.middleware.session.cookie :as cookie-session]
    [ring.middleware.webjars :as webjars-middleware]
    [ring.util.response :as response]
    [selmer.parser :as selmer]
    [timi.server.identity.authn-authz :as timi-authn-authz]
    [timi.server.identity.core :as timi-identity]
    [timi.server.datasource.core :as datasource]
    [timi.server.web.middleware :as timi-middleware]
    [timi.server.web.routes :as routes]))

(defn wrap-with-logger
  [handler config]
  (if (get-in config [:log :http-requests?])
    (if-url-doesnt-match
      handler
      (re-pattern (get-in config [:log :http-skip]))
      logger-middleware/wrap-with-logger)
    handler))

(defn app
  [config]
  (if (get config :selmer-caching? true)
    (selmer/cache-on!)
    (selmer/cache-off!))
  (let [auth-backend (timi-authn-authz/get-auth-backend config)]
    (-> routes/all
        (timi-identity/wrap-augment-identity)
        (timi-authn-authz/wrap-authn-and-authz auth-backend)
        (timi-middleware/wrap-session config)
        (params-middleware/wrap-params)
        (timi-middleware/wrap-no-cache)
        (timi-middleware/wrap-default-responses)
        (timi-middleware/wrap-selmer)
        (datasource/wrap-persistence config)
        (webjars-middleware/wrap-webjars)
        (wrap-with-logger config))))
