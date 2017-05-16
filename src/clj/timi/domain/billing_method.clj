(ns timi.domain.billing-method)

(def billing-method? #{:fixed-price :overhead :hourly})
(def billable? #{:hourly})
