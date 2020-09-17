(ns dlt.core
  (:require
   [datalevin.core :as d]
   [clojure.java.shell :as sh]))


(def db-file "/tmp/my-datalevin-dlt-test")
(def schema
  {:name          {:db/valueType :db.type/string}
   :dt/updated-at {:db/valueType :db.type/instant}})

(comment
  ;; drops the database
  (sh/sh "rm" "-rf" db-file)

  ;; create connection
  (def conn (d/create-conn db-file schema))

  ;; add initial data - seems to work fine
  (d/transact! conn [{:db/id         -1 :name "Namebo"
                      :dt/updated-at (java.util.Date.)}])

  ;; list added data
  (d/datoms @conn :eavt)

  ;; close the connection
  (d/close conn)

  ;; create new connection
  (def conn-2 (d/create-conn db-file schema))

  ;; notice that this connection appears to be an empty database
  ;; - no indexes
  ;; - max-eid 0
  conn-2

  ;; attempts to add a date here fails when deleting old data
  ;; (long vs date type)
  (d/transact! conn-2 [{:db/id         -2 :name "Another name"
                        :dt/updated-at (java.util.Date.)}])

  ;; Unhandled clojure.lang.ExceptionInfo
  ;;    Fail to transact to LMDB: "\"Error putting r/w key buffer of datalevin/eav:
  ;;    java.lang.Long cannot be cast to java.util.Date\""
  ;;    {:txs
  ;;     [[:del
  ;;       "datalevin/eav"
  ;;       #object[datalevin.bits.Indexable 0x6234bedb "datalevin.bits.Indexable@6234bedb"]
  ;;       :eav] .... etc

  ;; add a new item overwrites the existing entity at :id 1
  (d/transact! conn-2 [{:db/id -2 :name "Another name"}])

  ;; see that "Namebo" is gone, only "Another name" exists
  (d/datoms @conn-2 :eavt)
  )
