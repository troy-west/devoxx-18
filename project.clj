(defproject com.troy-west/devoxx-18 "0.1.0-SNAPSHOT"

  :description "Streaming Data Platforms & Clojure Live Demo"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.logging "0.4.0"]

                 [com.troy-west/arche-async "0.4.2"]
                 [com.troy-west/arche-hugcql "0.4.2" :exclusions [org.clojure/tools.reader]]
                 [com.troy-west/arche-integrant "0.4.2"]
                 [com.troy-west/thimble-all "0.1.9"]
                 [cc.qbits/alia-joda-time "4.1.1" :exclusions [joda-time]]

                 [cheshire "5.8.0"]
                 [clj-time "0.14.3"]
                 [metrics-clojure "2.10.0" :exclusions [org.clojure/clojure]]
                 [metrics-clojure-riemann "2.10.0" :exclusions [org.clojure/clojure]]
                 [ch.qos.logback/logback-classic "1.2.3"]])
