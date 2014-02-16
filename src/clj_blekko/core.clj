(ns clj-blekko.core
  (:require [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-http.client :as client]
            (org.bovinegenius [exploding-fish :as uri])))

(def *url-base* "http://blekko.com/ws/")

(def *last-request* (atom nil))

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

           cur-time      (time/now)
           
           result        (do
                           (Thread/sleep
                            (if @*last-request*
                              (time/minus
                               (time/plus @*last-request*
                                          (time/seconds 10))
                               cur-time)
                              0))
                           (-> query-url client/get :body))]

       (if (:json options-map)
         (json/parse-string result true)
         result))))
