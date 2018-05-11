(ns devoxx.query
  (:refer-clojure :exclude [partition])
  (:require [clojure.core.async :as async]
            [troy-west.arche :as arche]
            [troy-west.arche.async :as arche.async]
            [clj-time.core :as time]
            [clj-time.periodic :as periodic]
            [clj-time.coerce :as time.coerce]
            [clj-time.format :as time.format]))

(defn partition
  [date-time]
  (time.format/unparse (time.format/formatter "yyyy-MM-dd") date-time))

(defn queries
  [sender word start end]
  (let [days (periodic/periodic-seq (time.coerce/to-date-time start)
                                    (time/plus (time.coerce/to-date-time end) (time/days 1))
                                    (time/days 1))]
    (map (fn [day]
           {:sender sender
            :date   (partition day)
            :word   word})
         days)))

(defn read-day
  "Read a single day of messages for a sender"
  [connection sender date]
  (arche/execute
   connection
   :message/read
   {:values {:sender sender
             :date   (partition (time.coerce/to-date-time date))}}))

(defn search
  "Search for messages with a particular subject token and sender"
  ([connection sender word start end]
   (search connection sender word start end 25))
  ([connection sender word start end parallelism]
   (let [out (async/chan parallelism)]
     (async/pipeline-async
      parallelism
      out
      #(arche.async/execute connection :message/search {:values %1 :channel %2})
      (async/to-chan (queries sender word start end)))
     (async/<!! (async/into [] out)))))