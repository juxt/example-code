(ns fred.web
  (:require
   [datomic.api :as d]
   [org.httpkit.server :refer (run-server with-channel on-close send!)]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :refer :all]
   [liberator.core :refer (resource)]
   [hiccup.core :refer (html h)]
   [fred.data :refer (get-orders)]
   [clojure.core.async :as async :refer (go-loop)]))

(defn server-event-source [shared-ch]
  (let [multiplexer (async/mult shared-ch)]
    (fn [req]
      (let [ch (async/chan 16)]
        (async/tap multiplexer ch)
        (with-channel req channel
          (on-close channel
                    (fn [_] (async/close! ch)))
          (send! channel
                 {:headers {"Content-Type" "text/event-stream"}} false)
          (go-loop []
            (when-let [data (<! ch)]
              (infof "Sending browser this message: %s" data)
              (send! channel
                     (str "data: " data "\r\n\r\n")
                     false)
              (recur))))))))

(defrecord Web [conn source]
  component/Lifecycle
  (start [component]
    (let [events-handler (server-event-source (:ch source))]
      (assoc component
        :server (run-server
                 (fn [req]
                   (cond (= (:uri req) "/events")
                         (events-handler req)
                         :otherwise
                         ((resource
                           :available-media-types #{"text/plain" "text/html" "text/csv"}
                           :exists? (fn [ctx] true)
                           :handle-not-found "GO AWAY!!! NOT HERE"
                           :handle-ok
                           (fn [ctx]
                             (try
                               (case (-> ctx :representation :media-type)
                                 "text/plain" "Hello"
                                 "text/html"
                                 (html [:body
                                        [:div
                                         [:script {:lang "javascript"}
                                          (slurp "events.js")
                                          ]
                                         [:h1 "Order Book"]
                                         [:table {:border 2}
                                          (let [db (d/db (:conn conn))]
                                            (for [order (get-orders db)]
                                              [:tr
                                               (map #(identity [:td %])
                                                    ((juxt :fx/label :fx/rate :fx/amount)
                                                     order))]))]
                                         [:p "The current rate is: " [:span#rate]]
                                         ]])
                                 "text/csv" (->> (get-orders (d/db (:conn conn)))
                                                 (map (juxt :fx/label :fx/rate :fx/amount))
                                                 (map (partial interpose ","))
                                                 (map (partial apply str))
                                                 (interpose "\n")
                                                 (apply str)))
                               (catch Exception e (h (pr-str e)))))) req)))
                 {:port 3000}))))
  (stop [component]
    (when-let [server (:server component)]
      (server))
    (dissoc component :server)))

(def new-web-schema {})

(defn new-web [& {:as opts}]
  (component/using
   (->> opts (merge {}) map->Web)
   [:conn :source]))
