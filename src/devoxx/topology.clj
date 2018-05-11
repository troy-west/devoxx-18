(ns devoxx.topology
  (:refer-clojure :exclude [partition])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [troy-west.arche :as arche]
            [cheshire.core :as json]
            [clj-time.format :as time.format]
            [clj-time.coerce :as time.coerce])
  (:import (org.apache.kafka.streams KafkaStreams StreamsConfig StreamsBuilder)
           (org.apache.kafka.streams.kstream ForeachAction)))

(defn partition
  [date-time]
  (time.format/unparse (time.format/formatter "yyyy-MM-dd") date-time))

(defn tokens
  [subject]
  (->> (str/split subject #" ")
       (map str/lower-case)
       (filter (complement str/blank?))))

(defn process
  [cassandra text]
  (try
    (let [{:keys [message-id date to from subject]} (json/decode text true)]

      ;; log some output of process (every 1k messages logged)
      (when (= 0 (mod (Integer/parseInt message-id) 1000))
        (log/info "processed 1000 messages" message-id))

      ;; write a materialized view for every subject token found
      (doseq [token (tokens subject)]
        (let [view {:sender     from
                    :date       (partition (time.coerce/to-date-time date))
                    :word       token
                    :message-id message-id
                    :recipient  to
                    :subject    subject}]
          (arche/execute cassandra :message/write {:values view}))))
    (catch Throwable thr
      (log/error thr))))

(defn start
  [{:keys [topic stream cassandra]}]
  (let [builder (StreamsBuilder.)]
    (.foreach (.stream builder ^String topic)
              (reify ForeachAction
                (apply [_ _ text]
                  (process cassandra text))))
    (let [k-stream (KafkaStreams. (.build builder) (StreamsConfig. stream))]
      (.start k-stream)
      k-stream)))

(defn stop
  [k-stream]
  (.close ^KafkaStreams k-stream))