(ns clj-blekko.core
  (:require [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-http.client :as client]
            (org.bovinegenius [exploding-fish :as uri])))

(def *url-base* "http://blekko.com/ws/")

(def *last-request* nil)

(defn wait-a-sec
  []
  (let [cur-time (time/now)]))

(defn run-query
  "Rate limited to 1 query per second"
  ([q auth]
     (run-query q auth nil))

  ([q auth & options]
     (let [options-map   (into {} [(into [] options)])

           encoded-query (if (:json options-map)
                           (str q " /json /ps=100")
                           (str q " /ps=100"))
           query-url     (-> *url-base*
                             (uri/param "q" encoded-query)
                             (uri/param "auth" auth))

           result        (-> query-url client/get :body)]
       (if (:json options-map)
         (json/parse-string result true)
         result))))
