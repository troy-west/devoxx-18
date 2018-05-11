# DevoxxUK 2018: Streaming Data Platforms & Clojure

Toward the end of the talk we live-code a streaming data platorm, and:

1. Ingest a simplified, pared back version of the (public domain) Enron corpus (100k events)
2. Transform those events into materialized views based on [Datastax best practice for partitioned time-series](https://academy.datastax.com/resources/getting-started-time-series-data-modeling)
3. Retain those events by writing them to Cassandra
4. Retrieve events by day, and search for events by subject

To reproduce for yourself:

Install CCM (Cassandra Cluster Manager)

```bash
brew install ccm
```

Initialize a new single-node cluster

```bash
ccm create -n 1 -v 3.0.15 devoxx
```

then start a REPL and

```clojure
;; load simplified Enron data into the REPL

(def messages (json/decode (slurp (io/resource "messages.json")) true))

;; require system ns and initialize zookeeper, kafka, kafka-streams, and cassandra

(require '[devoxx.system :as system])

(system/initialize)

;; send the test messages to Kafka

(system/send (:thimble/kafka.producer @system/state) "devoxx" messages)

;; require query ns and retrieve data

(require '[devoxx.query :as query])

;; load a single day of messages for a sender

(query/read-day (:arche/connection @system/state)
                "mark" 
                "1998-12-02T")

;; load a time series of messages for a user where the message subject contained a specific word

(query/search (:arche/connection @system/state)
              "mark"
              "swap"
              "1998-12-02T"
              "2001-12-22T")
```

Any problems - shout.
