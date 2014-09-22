(ns fred.data
  (:require
   [datomic.api :as d]
   [com.stuartsierra.component :refer (Lifecycle)]
   [schema.core :as s]))

(defn transact-fx-rate [conn ident rate]
  @(d/transact conn [[:db/add ident :fx/rate rate]]))

(defn get-current-rate [db ccypair]
  (d/q
   '[:find ?rate
     :where
     [?fred :fx/rate ?rate]
     [?fred :fx/label ccypair]]
   db))

(defn transact-new-fx-order [conn & {:keys [ccypair rate amount expiry direction]}]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [[:db/add id :fx/label ccypair]
                       [:db/add id :fx/amount amount]
                       [:db/add id :fx/expiry expiry]
                       [:db/add id :fx/direction
                        (case direction
                          :buy :fx/buy
                          :sell :fx/sell
                          (throw (ex-info "Invalid direction" {:direction direction})))]
                       [:db/add id :fx/rate rate]])))

(defn get-orders [db]
  (map (comp (partial d/entity db) first)
       (d/q '[:find ?order
              :where
              [?order :fx/expiry ?expiry]]
            db)))

(defn transact-schema [conn]
  @(d/transact
    conn
    [{:db/id #db/id[:db.part/db]
      :db/ident :fx/label
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db/doc "Used to as the currency pair designation"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :fx/rate
      :db/valueType :db.type/double
      :db/cardinality :db.cardinality/one
      :db/doc "FX rate"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :fx/amount
      :db/valueType :db.type/double
      :db/cardinality :db.cardinality/one
      :db/doc "Amount of base currency to trade"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :fx/expiry
      :db/valueType :db.type/instant
      :db/cardinality :db.cardinality/one
      :db/doc "Expiry date"
      :db.install/_attribute :db.part/db}

     {:db/id #db/id[:db.part/db]
      :db/ident :fx/direction
      :db/valueType :db.type/keyword
      :db/cardinality :db.cardinality/one
      :db/doc "Direction"
      :db.install/_attribute :db.part/db}

     ]))

(defn transact-ccy [conn label ident]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn
                 [{:db/id id
                   :db/ident ident
                   :fx/label label}])))

(defn transact-ccys [conn]
  (transact-ccy conn "AUD/USD" :aussie)
  (transact-ccy conn "EUR/GBP" :chunnel)
  (transact-ccy conn "EUR/JPY" :yuppy)
  (transact-ccy conn "EUR/USD" :euro)
  (transact-ccy conn "GBP/JPY" :geppy)
  (transact-ccy conn "GBP/USD" :cable)
  (transact-ccy conn "NZD/USD" :kiwi)
  (transact-ccy conn "USD/CAD" :loonie)
  (transact-ccy conn "USD/CHF" :swissy)
  (transact-ccy conn "USD/JPY" :ninja))

(defrecord DatomicConnection [uri]
  Lifecycle
  (start [component]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)]
      (transact-schema conn)
      (transact-ccys conn)

      (transact-new-fx-order conn
                             :ccypair "EUR/GBP"
                             :rate 0.8
                             :amount (double 10)
                             :expiry (java.util.Date.)
                             :direction :buy
                             )

      (transact-new-fx-order conn
                             :ccypair "EUR/GBP"
                             :rate 0.9
                             :amount (double 10)
                             :expiry (java.util.Date.)
                             :direction :buy
                             )

      (transact-new-fx-order conn
                             :ccypair "EUR/GBP"
                             :rate 0.7
                             :amount (double 10)
                             :expiry (java.util.Date.)
                             :direction :buy
                             )

      (assoc component :conn conn)))
  (stop [component]
    (d/release (:conn component))
    component))

(def new-datomic-connection-schema {:uri s/Str})

(defn new-datomic-connection [& {:as opts}]
  (->> opts
       (merge {})
       (s/validate new-datomic-connection-schema)
       map->DatomicConnection))





#_(let [conn (-> dev/system :md-datomic-connection :conn)]
  (->>  (get-orders (d/db conn))
        (map (juxt :fx/label :fx/rate :fx/amount))
        (map #(interpose "," %))
        (map (partial apply str))
        (interpose "\n")
        (apply str)
        )


  #_(transact-new-fx-order conn
                           :ccypair "EUR/GBP"
                           :rate 0.8
                           :amount (double 10)
                           :expiry (java.util.Date.)
                           :direction :buy
                           )    )
