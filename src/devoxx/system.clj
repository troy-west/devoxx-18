(ns devoxx.system
  (:refer-clojure :exclude [send])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [troy-west.thimble]
            [troy-west.thimble.kafka :as thimble.kafka]
            [troy-west.arche.hugcql :as arche.hugcql]
            [troy-west.arche.integrant :as arche.integrant]
            [devoxx.topology :as topology]
            [qbits.alia.codec.extension.joda-time :refer :all]
            [cheshire.core :as json]
            [integrant.core :as ig]))

(defonce state (atom nil))

(defmethod ig/init-key :devoxx/stream
  [_ config]
  (topology/start config))

(defmethod ig/halt-key! :devoxx/stream
  [_ config]
  (topology/stop config))

(defn initialize
  []
  (let [config (edn/read-string {:readers (merge arche.hugcql/data-readers
                                                 arche.integrant/data-readers)}
                                (slurp (io/resource "config.edn")))]
    (when-not @state
      (reset! state (ig/init config)))))

(defn shutdown
  []
  (when @state
    (ig/halt! @state)
    (reset! state nil)))

(defn send
  [producer topic messages]
  (log/infof "sending %s messages" (count messages))
  (doseq [message messages]
    (thimble.kafka/send-message producer
                                topic
                                (:message-id message)
                                (json/encode message)))
  (log/info "messages sent"))