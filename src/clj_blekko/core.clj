(ns clj-blekko.core
  (:require [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clj-http.client :as client]
            (org.bovinegenius [exploding-fish :as uri])))

(def *url-base* "http://blekko.com/ws/")

(def *last-request* (atom nil))

(def *delay-secs* 1)

(defn run-query
  "Rate limited to 1 query per second"
  ([q auth]
     (run-query q auth nil))

  ([q auth & options]
     (let [options-map   (into {} [(into [] options)])

           encoded-query (if (:json options-map)
                           (str q " /json /ps=100")
                           (str q " /ps=100"))
           query-url     (if-not (:page options-map)
                           (-> *url-base*
                               (uri/param "q" encoded-query)
                               (uri/param "auth" auth))
                           (-> *url-base*
                               (uri/param "q" encoded-query)
                               (uri/param "auth" auth)
                               (uri/param "p" (:page options-map))))
           
           wait-for      (if @*last-request*
                           (- (coerce/to-long
                               (time/plus @*last-request*
                                          (time/seconds *delay-secs*)))
                              (coerce/to-long (time/now))) 0)
           
           result        (do
                           (when (< 0 wait-for)
                             (Thread/sleep wait-for))
                           (swap! *last-request* (fn [x] (time/now)))
                           (-> query-url client/get :body))]

       (if (:json options-map)
         (json/parse-string result true)
         result))))
