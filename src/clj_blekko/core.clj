(ns clj-blekko.core
  (:require [clj-time.core :as time]
            [clj-http.client :as client]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [java.net URLEncoder]))

(def *url-base* "http://blekko.com/ws/")

(def *last-request* nil)

(defn wait-a-sec
  []
  (let [cur-time (time/now)]))

(defn run-query
  "Rate limited to 1 query per second"
  [q auth]
  (let [encoded-query (URLEncoder/encode q)
        query-url     (-> *url-base*
                          (uri/param "q" encoded-query)
                          (uri/param "auth" auth))]
    (client/get query-url)))
