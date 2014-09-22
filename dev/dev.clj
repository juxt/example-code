(ns dev
  (:require
   [fred.system :refer (new-system)]
   [com.stuartsierra.component :as c]
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]))

(def system nil)

(defn init
  "Constructs the system."
  []
  (alter-var-root #'system
    (constantly (new-system))))

(defn start
  "Starts the system."
  []
  (alter-var-root
   #'system
   #(c/start %)))

(defn stop
  "Shuts down and destroys the system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (c/stop s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start)
  :ok
  )

(defn reset []
  (stop)
  (refresh :after 'dev/go))
