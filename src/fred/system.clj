(ns fred.system
  (:require
   [com.stuartsierra.component :as c]
   [fred.data :refer (new-datomic-connection)]
   [fred.md :as md]
   [fred.web :refer (new-web)]
   [clojure.tools.logging :refer :all]))

(defn new-system
  "Create a new system"
  []
  (c/system-using
   (c/system-map
    :market-data-source (md/new-market-data-source)
;;    :market-data-consumer (md/new-market-data-consumer)
    :md-datomic-connection (new-datomic-connection :uri "datomic:mem://hsbc/fx/md")
    :web (new-web))

   {#_:market-data-consumer #_{:source :market-data-source
                               :conn :md-datomic-connection}
    :web {:conn :md-datomic-connection
          :source :market-data-source}}))
