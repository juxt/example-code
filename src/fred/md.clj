(ns fred.md
  (:require
   [com.stuartsierra.component :as c]
   [clojure.tools.logging :refer :all]
   [clojure.core.async :as a]))



(defrecord MarketDataSource [shutdown]
  c/Lifecycle
  (start [component]
    (let [ch (a/chan)]
      (a/go-loop []
        (a/<! (a/timeout 100))
        (let [r (a/alts! [shutdown [ch (double (/ (+ 76 (* 8 (rand))) 100))]])]
          (when-not (= shutdown (second r))
            (recur))))
      (assoc component :ch ch)))
  (stop [component]
    (a/close! shutdown)
    (a/close! (:ch component))
    component))

(defn new-market-data-source [& {:as opts}]
  (->> opts
       (merge {:shutdown (a/chan)})
       map->MarketDataSource))

(defrecord MarketDataConsumer [source conn]
  c/Lifecycle
  (start [component]
    (a/go-loop []
      (if-let [msg (a/<! (:ch source))]
        (do
          (infof "We took this: %s" msg)
          (recur))
        (infof "We took a nil! terminating")))
    component)
  (stop [component] component))

(defn new-market-data-consumer [& {:as opts}]
  (let [component (->> opts
                       (merge {})
                       map->MarketDataConsumer)]
    (c/using component [:source :conn])))
