(defproject fred "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [com.stuartsierra/component "0.2.1"]

                 [com.datomic/datomic-free "0.9.4815.12"
                  #_:exclusions #_[org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12
                               com.amazonaws/aws-java-sdk

                               org.slf4j/jul-to-slf4j
                               org.slf4j/log4j-over-slf4j
                               org.slf4j/slf4j-api
                               org.slf4j/jcl-over-slf4j
                               ]]

                 [ch.qos.logback/logback-classic "1.0.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.2"]
                 [org.slf4j/jcl-over-slf4j "1.7.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]

                 [prismatic/schema "0.2.1"]

                 [http-kit "2.1.13"]
                 [liberator "0.12.1"]
                 ]

  :profiles {:dev {:source-paths ["dev"]}}

  )
